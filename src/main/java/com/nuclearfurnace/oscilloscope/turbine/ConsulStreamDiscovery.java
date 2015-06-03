package com.nuclearfurnace.oscilloscope.turbine;

import com.ecwid.consul.v1.ConsistencyMode;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.CatalogClient;
import com.ecwid.consul.v1.catalog.model.*;
import com.netflix.turbine.discovery.StreamAction;
import com.netflix.turbine.discovery.StreamDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ConsulStreamDiscovery implements StreamDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ConsulStreamDiscovery.class);

    private final String clusterName;
    private final String uriTemplate;

    public ConsulStreamDiscovery(String clusterName, String uriTemplate) {
        this.clusterName = clusterName;
        this.uriTemplate = uriTemplate;
    }

    public Observable<StreamAction> getInstanceList() {
        return getInstances()
                .map(ci -> {
                    URI uri;
                    try {
                        final String parsedUriTemplate = this.uriTemplate
                                .replace(StreamDiscoveryFactory.HOSTNAME, ci.getHostname())
                                .replace(StreamDiscoveryFactory.PORT, String.valueOf(ci.getPort()));

                        uri = new URI(parsedUriTemplate);
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid URI", e);
                    }

                    if (ci.getStatus() == ConsulInstance.Status.UP) {
                        return StreamAction.create(StreamAction.ActionType.ADD, uri);
                    } else {
                        return StreamAction.create(StreamAction.ActionType.REMOVE, uri);
                    }
                });
    }

    private Observable<ConsulInstance> getInstances() {
        return Observable.
                create((Subscriber<? super ConsulInstance> subscriber) -> {
                    try {
                        // The terminology is slightly confusing but "CatalogService" just refers to an instance of a service registered, overall, in the catalog,
                        // so we're asking for all nodes that provide the service, but only for ones with the "hystrix" tag, just in case one of them isn't running
                        // it for some reason.  We don't want to waste time connecting to someone that isn't running it.
                        ConsulClient client = new ConsulClient();
                        Response<List<CatalogService>> response = client.getCatalogService(this.clusterName, "hystrix", new QueryParams(ConsistencyMode.DEFAULT));
                        List<CatalogService> serviceInstances = response.getValue();

                        for(CatalogService si : serviceInstances) {
                            logger.debug("emitting up for {}:{}", si.getServiceAddress(), si.getServicePort());

                            // Since Consul will have health checks going, it's going to prune the list for us automatically.  Let the delta process handle this.
                            subscriber.onNext(new ConsulInstance(si.getServiceAddress(), si.getServicePort(), this.clusterName, ConsulInstance.Status.UP));
                        }

                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .toList()
                .repeatWhen(a -> a.flatMap(n -> Observable.timer(5, TimeUnit.SECONDS))) // Repeat this every 5 seconds.
                .startWith(new ArrayList<ConsulInstance>())
                .buffer(2, 1)
                .filter(l -> l.size() == 2)
                .flatMap(ConsulStreamDiscovery::delta);
    }

    private static Observable<ConsulInstance> delta(List<List<ConsulInstance>> listOfLists) {
        if (listOfLists.size() == 1) {
            return Observable.from(listOfLists.get(0));
        }

        // Get the difference of the two lists here.
        List<ConsulInstance> newList = listOfLists.get(1);
        List<ConsulInstance> oldList = new ArrayList<>(listOfLists.get(0));

        Set<ConsulInstance> delta = new LinkedHashSet<>();
        delta.addAll(newList);
        delta.removeAll(oldList);

        // Filter entries from newList out of oldList.
        oldList.removeAll(newList);

        // Create remove/drop actions for everyone left in oldList.
        for (ConsulInstance old : oldList) {
            logger.debug("emitting down for {}:{}", old.getHostname(), old.getPort());
            delta.add(new ConsulInstance(old.getHostname(), old.getPort(), old.getCluster(), ConsulInstance.Status.DOWN));
        }

        return Observable.from(delta);
    }
}
