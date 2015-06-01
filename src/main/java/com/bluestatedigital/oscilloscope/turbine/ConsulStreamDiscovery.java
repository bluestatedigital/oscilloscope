package com.bluestatedigital.oscilloscope.turbine;

import com.netflix.turbine.discovery.StreamAction;
import com.netflix.turbine.discovery.StreamDiscovery;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConsulStreamDiscovery implements StreamDiscovery {
    public Observable<ConsulInstance> getInstances(String clusterName) {
        return Observable.
                create((Subscriber<? super ConsulInstance> subscriber) -> {
                    try {
                        subscriber.onNext(ConsulInstance.create(instance));
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .toList()
                .repeatWhen(a -> a.flatMap(n -> Observable.timer(5, TimeUnit.SECONDS))) // repeat after 30 second delay
                .startWith(new ArrayList<ConsulInstance>())
                .buffer(2, 1)
                .filter(l -> l.size() == 2)
                .flatMap(ConsulStreamDiscovery::delta);
    }

    static Observable<ConsulInstance> delta(List<List<ConsulInstance>> listOfLists) {
        if (listOfLists.size() == 1) {
            return Observable.from(listOfLists.get(0));
        } else {
            // diff the two
            List<EurekaInstance> newList = listOfLists.get(1);
            List<EurekaInstance> oldList = new ArrayList<>(listOfLists.get(0));

            Set<EurekaInstance> delta = new LinkedHashSet<>();
            delta.addAll(newList);
            // remove all that match in old
            delta.removeAll(oldList);

            // filter oldList to those that aren't in the newList
            oldList.removeAll(newList);

            // for all left in the oldList we'll create DROP events
            for (EurekaInstance old : oldList) {
                delta.add(EurekaInstance.create(Status.DOWN, old.getInstanceInfo()));
            }

            return Observable.from(delta);
        }
}
