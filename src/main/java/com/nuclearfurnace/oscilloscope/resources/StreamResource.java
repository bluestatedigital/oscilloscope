package com.nuclearfurnace.oscilloscope.resources;

import com.nuclearfurnace.oscilloscope.turbine.StreamDiscoveryFactory;
import com.netflix.turbine.aggregator.InstanceKey;
import com.netflix.turbine.aggregator.StreamAggregator;
import com.netflix.turbine.aggregator.TypeAndNameKey;
import com.netflix.turbine.discovery.StreamAction;
import com.netflix.turbine.discovery.StreamDiscovery;
import com.netflix.turbine.internal.JsonUtility;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import org.eclipse.jetty.io.EofException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.observables.GroupedObservable;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Path("/stream")
@Produces("text/event-stream")
public class StreamResource
{
    private static final Logger logger = LoggerFactory.getLogger(StreamResource.class);

    private final StreamDiscoveryFactory discoveryFactory;

    public StreamResource(StreamDiscoveryFactory discoveryFactory) {
        this.discoveryFactory = discoveryFactory;
    }

    @GET
    @Path("host")
    public Response streamHost(@QueryParam("target") String target)
    {
        // Make sure the caller gave us a target.
        if(target == null || target.isEmpty()) {
            return getClientError("'target' must be specified e.g. 127.0.0.1:8090/hystrix.stream");
        }

        // Make sure we have a valid target e.g. scheme.
        StringBuilder targetUrlBuilder = new StringBuilder();
        if(!target.startsWith("http://")) {
            targetUrlBuilder.append("http://");
        }

        targetUrlBuilder.append(target);

        // Generate a URI from it.  Default to '/hystrix.stream' if a path wasn't given.
        URI uri = null;

        try {
            uri = URI.create(targetUrlBuilder.toString());
            if(uri.getPath().isEmpty()) {
                targetUrlBuilder.append("/hystrix.stream");
                uri = URI.create(targetUrlBuilder.toString());
            }
        }
        catch (Exception e)
        {
            return getClientError("target must be valid");
        }

        if(!isValidUrl(uri)) {
            return getClientError("target must be valid");
        }

        // Get our stream and convert it to a streaming response.
        Observable<Map<String, Object>> eventStream = getEventStream(uri, Observable.never());
        return streamFromObservable(eventStream);
    }

    @GET
    @Path("cluster")
    public Response streamCluster(@QueryParam("cluster") String cluster)
    {
        // Get a stream discovery for the given cluster.
        StreamDiscovery discovery = this.discoveryFactory.getDiscoveryForCluster(cluster);

        // Get our stream and convert it to a streaming response.
        Observable<Map<String, Object>> eventStream = getGroupedStream(discovery)
                .flatMap(o -> o);

        return streamFromObservable(eventStream);
    }

    /**
     * Builds a streaming response based on an observable.
     *
     * @param events the observable to stream
     * @return a Response that uses a steaming output
     */
    private static Response streamFromObservable(Observable<Map<String, Object>> events) {
        final StreamingOutput streamingOutput = outputStream -> {
            logger.info("Starting streaming response of event stream to client...");

            final CountDownLatch countDownLatch = new CountDownLatch(1);

            events.subscribe(new Subscriber<Map<String, Object>>() {
                @Override
                public void onCompleted() {
                    try {
                        logger.debug("Event stream finished; closing stream to client.");
                        outputStream.close();
                    } catch (IOException e) {
                        logger.warn("Exception closing output stream", e);
                    } finally {
                        countDownLatch.countDown();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    logger.error("Error streaming events", e);
                }

                @Override
                public void onNext(Map<String, Object> event) {
                    try {
                        String eventAsJson = JsonUtility.mapToJson(event);
                        logger.debug("Writing event to stream; event: {} bytes", eventAsJson.length());

                        outputStream.write("data: ".getBytes());
                        outputStream.write(eventAsJson.getBytes());
                        outputStream.write("\n\n".getBytes());
                        outputStream.flush();
                    } catch (EofException e) {
                        logger.debug("Client disconnected; unsubscribing from stream...");

                        unsubscribe();
                        countDownLatch.countDown();
                    }
                    catch(Exception e) {
                        logger.warn("Unexpected exception during streaming", e);

                        unsubscribe();
                        countDownLatch.countDown();
                    }
                }
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                logger.warn("Current thread interrupted, resetting flag");
                Thread.currentThread().interrupt();
            }
        };

        return Response.ok(streamingOutput).header("Access-Control-Allow-Origin", "*").build();
    }

    /**
     * Connects to a single event stream and manages retrying the stream if it fails.
     *
     * @param uri the target to connect to
     * @param removals an Observable that emits the names of
     * @return an observable that emits server-sent events from the target URI
     */
    public static Observable<Map<String, Object>> getEventStream(URI uri, Observable<StreamAction> removals) {
        return Observable.defer(() -> {
            Observable<Map<String, Object>> eventStream = RxNetty.createHttpClient(uri.getHost(), uri.getPort(), PipelineConfigurators.<ByteBuf>clientSseConfigurator())
                    .submit(HttpClientRequest.createGet(uri.toASCIIString()))
                    .flatMap(response -> {
                        // Make sure we got a 200 OK response.
                        if (response.getStatus().code() != 200) {
                            return Observable.error(new RuntimeException("Failed to connect to individual stream '" + uri + "': " + response.getStatus()));
                        }

                        // Make sure this is an event stream.
                        if (!isEventStream(response)) {
                            return Observable.error(new RuntimeException("Stream is not an event stream: " + uri));
                        }

                        // Pass back an unsubscribable observable so we can stop streams in cluster mode.
                        return response.getContent()
                                .doOnSubscribe(() -> logger.debug("Subscribed to individual stream: " + uri))
                                .doOnUnsubscribe(() -> logger.debug("Unsubscribed from individual stream: " + uri))
                                .takeUntil(removals.filter(a -> a.getUri().equals(uri)))
                                .map(sse -> JsonUtility.jsonToMap(sse.contentAsString()));
                    });

            return eventStream.retryWhen(attempts ->
                    attempts.flatMap(e ->
                            Observable.timer(1, TimeUnit.SECONDS)
                                    .doOnEach(n -> logger.debug("Individual stream ended prematurely; retrying in 1 second: " + uri))));
        });
    }

    public static Observable<GroupedObservable<TypeAndNameKey, Map<String, Object>>> getGroupedStream(StreamDiscovery discovery) {
        Observable<StreamAction> streamActions = discovery.getInstanceList().publish().refCount();
        Observable<StreamAction> streamAdditions = streamActions.filter(a -> a.getType() == StreamAction.ActionType.ADD);
        Observable<StreamAction> streamRemovals = streamActions.filter(a -> a.getType() == StreamAction.ActionType.REMOVE);

        Observable<GroupedObservable<InstanceKey, Map<String, Object>>> streamPerInstance =
                streamAdditions.map(streamAction -> {
                    URI uri = streamAction.getUri();
                    return GroupedObservable.from(InstanceKey.create(uri.toASCIIString()), getEventStream(uri, streamRemovals));
                });

        return StreamAggregator.aggregateGroupedStreams(streamPerInstance);
    }


    public static boolean isValidUrl(URI uri) {
        return true;
    }

    public static Response getClientError(String errorMessage) {
        return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
    }

    public static <R> boolean isEventStream(HttpClientResponse<R> response) {
        return Arrays.asList(response.getHeaders().getHeader(HttpHeaders.CONTENT_TYPE, "").split(";")).contains("text/event-stream");
    }
}
