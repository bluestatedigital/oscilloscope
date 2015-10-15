package com.nuclearfurnace.oscilloscope.discovery.providers;

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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class StaticConfigurationClusterProvider implements ClusterProvider {
    final DynamicStringProperty staticClusters = DynamicPropertyFactory.getInstance().getStringProperty("oscilloscope.discovery.providers.static", "");
    final DynamicStringProperty uriTemplate = DynamicPropertyFactory.getInstance().getStringProperty("oscilloscope.discovery.providers.static.uri_template", "http://{HOSTNAME}:{PORT}/hystrix.stream");

    @Override
    public Observable<Cluster> getClusters() {
        final String[] staticClusterNames = staticClusters.get().split(",");

        return Observable.from(staticClusterNames)
                .map(name -> new Cluster(name, "static"));
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
                        final List<ClusterInstance> clusterInstances = getInstanceListForCluster(clusterName);

                        for(ClusterInstance clusterInstance : clusterInstances) {
                            subscriber.onNext(clusterInstance);
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
                .flatMap(StaticConfigurationClusterProvider::instanceDelta);
    }

    private static Observable<ClusterInstance> instanceDelta(List<List<ClusterInstance>> listOfLists) {
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

    private static List<ClusterInstance> getInstanceListForCluster(String clusterName) {
        final String[] rawInstances = DynamicPropertyFactory.getInstance().getStringProperty(String.format("oscilloscope.discovery.providers.static.%s", clusterName), "").get().split(",");

        ArrayList<ClusterInstance> clusterInstances = new ArrayList<>();
        for(String rawInstance : rawInstances) {
            String[] rawInstanceParts = rawInstance.split(":");
            if(rawInstanceParts.length != 2) {
                // We expect [hostname / IP]:[port], and nothing else.
                continue;
            }

            final String hostname = rawInstanceParts[0];
            Integer port;
            try {
                port = Integer.parseInt(rawInstanceParts[1]);
            } catch(NumberFormatException ex) {
                // Need a real port.  Just skip.
                continue;
            }

            clusterInstances.add(new ClusterInstance(hostname, port, clusterName, ClusterInstance.Status.UP));
        }

        return clusterInstances;
    }
}
