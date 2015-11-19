package com.nuclearfurnace.oscilloscope.discovery.providers;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.*;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.turbine.discovery.StreamAction;
import com.nuclearfurnace.oscilloscope.discovery.Cluster;
import com.nuclearfurnace.oscilloscope.discovery.ClusterInstance;
import com.nuclearfurnace.oscilloscope.discovery.ClusterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ElasticLoadBalancerClusterProvider implements ClusterProvider {
    private final Logger logger = LoggerFactory.getLogger(ElasticLoadBalancerClusterProvider.class);

    final static DynamicStringProperty uriTemplate = DynamicPropertyFactory.getInstance().getStringProperty("oscilloscope.discovery.providers.elb.uri_template", "http://{HOSTNAME}:{PORT}/hystrix.stream");
    final static DynamicBooleanProperty usePrivateAddress = DynamicPropertyFactory.getInstance().getBooleanProperty("oscilloscope.discovery.providers.elb.use_private_address", true);
    final static DynamicStringProperty tagName = DynamicPropertyFactory.getInstance().getStringProperty("oscilloscope.discovery.providers.elb.tag_name", "features");

    @Override
    public Observable<Cluster> getClusters() {
        AmazonElasticLoadBalancing elbClient = new AmazonElasticLoadBalancingClient();
        DescribeLoadBalancersResult describeLoadBalancersResult = elbClient.describeLoadBalancers();

        return Observable.from(describeLoadBalancersResult.getLoadBalancerDescriptions())
                .buffer(20) // Can't describe more than 20 load balancers at a time... fuck you, Amazon.
                .flatMap(descriptions -> {
                    List<String> loadBalancerNames = descriptions.stream()
                            .map(description -> description.getLoadBalancerName())
                            .collect(Collectors.toList());

                    DescribeTagsRequest tagsRequest = new DescribeTagsRequest();
                    tagsRequest.setLoadBalancerNames(loadBalancerNames);
                    DescribeTagsResult describeTagsResult = elbClient.describeTags(tagsRequest);

                    List<Cluster> matchingLoadBalancers = describeTagsResult.getTagDescriptions().stream()
                            .filter(description -> description.getTags().stream().anyMatch(tag -> Objects.equals(tag.getKey(), tagName.get()) && tag.getValue().contains("hystrix")))
                            .map(description -> new Cluster(description.getLoadBalancerName(), "elb"))
                            .collect(Collectors.toList());

                    return Observable.from(matchingLoadBalancers);
                });
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
                .flatMap(ElasticLoadBalancerClusterProvider::instanceDelta);
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
        AmazonElasticLoadBalancing elbClient = new AmazonElasticLoadBalancingClient();
        AmazonEC2 ec2Client = new AmazonEC2Client();

        DescribeLoadBalancersRequest describeLoadBalancersRequest = new DescribeLoadBalancersRequest();
        describeLoadBalancersRequest.withLoadBalancerNames(clusterName);
        DescribeLoadBalancersResult describeLoadBalancersResult = elbClient.describeLoadBalancers(describeLoadBalancersRequest);

        return Observable.from(describeLoadBalancersResult.getLoadBalancerDescriptions())
                .flatMap(description -> {
                    Integer instancePort = description.getListenerDescriptions().get(0).getListener().getInstancePort();
                    return Observable.from(description.getInstances())
                            .map(instance -> new ElasticLoadBalancerClusterProvider().new EnhancedInstance(instance, instancePort));
                })
                .buffer(20)
                .flatMap(instances -> {
                    Integer instancePort = instances.get(0).InstancePort;
                    List<String> separatedInstances = instances.stream()
                            .map(instance -> instance.Instance.getInstanceId())
                            .collect(Collectors.toList());

                    DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
                    describeInstancesRequest.setInstanceIds(separatedInstances);

                    DescribeInstancesResult describeInstancesResult = ec2Client.describeInstances(describeInstancesRequest);
                    return Observable.from(describeInstancesResult.getReservations())
                            .flatMap(reservation -> Observable.from(reservation.getInstances()))
                            .map(instance -> {
                                String instanceAddress = instance.getPrivateIpAddress();
                                if(!usePrivateAddress.get()) {
                                    instanceAddress = instance.getPublicDnsName();
                                }

                                return new ClusterInstance(instanceAddress, instancePort, clusterName, ClusterInstance.Status.UP);
                            });
                })
                .toList()
                .toBlocking()
                .first();
    }

    private class EnhancedInstance {
        public final Instance Instance;
        public final Integer InstancePort;

        public EnhancedInstance(Instance instance, Integer instancePort) {
            Instance = instance;
            InstancePort = instancePort;
        }
    }
}
