package com.nuclearfurnace.oscilloscope.discovery.providers;

import com.ecwid.consul.v1.ConsistencyMode;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.turbine.discovery.StreamAction;
import com.nuclearfurnace.oscilloscope.discovery.Cluster;
import com.nuclearfurnace.oscilloscope.discovery.ClusterInstance;
import com.nuclearfurnace.oscilloscope.discovery.ClusterProvider;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ConsulClusterProvider implements ClusterProvider {
    final DynamicStringProperty uriTemplate = DynamicPropertyFactory.getInstance().getStringProperty("oscilloscope.discovery.providers.consul.uri_template", "http://{HOSTNAME}:{PORT}/hystrix.stream");

    public Observable<Cluster> getClusters() {
        HashSet<String> clusters = new HashSet<>();

        try {
            ConsulClient client = new ConsulClient();
            Response<Map<String, List<String>>> response = client.getCatalogServices(new QueryParams(ConsistencyMode.DEFAULT));
            for(Map.Entry<String, List<String>> entry : response.getValue().entrySet()) {
                if(entry.getValue().contains("hystrix")) {
                    clusters.add(entry.getKey());
                }
            }
        } catch(Exception e) {
        }

        return Observable.from(clusters)
                .map(cluster -> new Cluster(cluster, "consul"));
    }

    public Observable<StreamAction> getInstanceList(String clusterName) {
        return getInstances(clusterName)
                .map(ci -> {
                    URI uri;
                    try {
                        final String parsedUriTemplate = uriTemplate.get()
                                .replace("{HOSTNAME}", ci.getHostname())
                                .replace("{PORT}", String.valueOf(ci.getPort()));

                        uri = new URI(parsedUriTemplate);
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid URI", e);
                    }

                    if (ci.getStatus() == ClusterInstance.Status.UP) {
                        return StreamAction.create(StreamAction.ActionType.ADD, uri);
                    } else {
                        return StreamAction.create(StreamAction.ActionType.REMOVE, uri);
                    }
                });
    }

    private Observable<ClusterInstance> getInstances(String clusterName) {
        return Observable.
                create((Subscriber<? super ClusterInstance> subscriber) -> {
                    try {
                        // The terminology is slightly confusing but "CatalogService" just refers to an instance of a service registered, overall, in the catalog,
                        // so we're asking for all nodes that provide the service, but only for ones with the "hystrix" tag, just in case one of them isn't running
                        // it for some reason.  We don't want to waste time connecting to someone that isn't running it.
                        ConsulClient client = new ConsulClient();
                        Response<List<CatalogService>> response = client.getCatalogService(clusterName, "hystrix", new QueryParams(ConsistencyMode.DEFAULT));
                        List<CatalogService> serviceInstances = response.getValue();

                        for(CatalogService si : serviceInstances) {
                            // Since Consul will have health checks going, it's going to prune the list for us automatically.  Let the delta process handle this.
                            subscriber.onNext(new ClusterInstance(si.getServiceAddress(), si.getServicePort(), clusterName, ClusterInstance.Status.UP));
                        }

                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .toList()
                .repeatWhen(a -> a.flatMap(n -> Observable.timer(5, TimeUnit.SECONDS))) // Repeat this every 5 seconds.
                .startWith(new ArrayList<ClusterInstance>())
                .buffer(2, 1)
                .filter(l -> l.size() == 2)
                .flatMap(ConsulClusterProvider::delta);
    }

    private static Observable<ClusterInstance> delta(List<List<ClusterInstance>> listOfLists) {
        if (listOfLists.size() == 1) {
            return Observable.from(listOfLists.get(0));
        }

        // Get the difference of the two lists here.
        List<ClusterInstance> newList = listOfLists.get(1);
        List<ClusterInstance> oldList = new ArrayList<>(listOfLists.get(0));

        Set<ClusterInstance> delta = new LinkedHashSet<>();
        delta.addAll(newList);
        delta.removeAll(oldList);

        // Filter entries from newList out of oldList.
        oldList.removeAll(newList);

        // Create remove/drop actions for everyone left in oldList.
        for (ClusterInstance old : oldList) {
            delta.add(new ClusterInstance(old.getHostname(), old.getPort(), old.getCluster(), ClusterInstance.Status.DOWN));
        }

        return Observable.from(delta);
    }
}
