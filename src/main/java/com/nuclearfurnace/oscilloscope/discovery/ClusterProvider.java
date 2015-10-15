package com.nuclearfurnace.oscilloscope.discovery;

import com.netflix.turbine.discovery.StreamAction;
import rx.Observable;

public interface ClusterProvider {
    Observable<Cluster> getClusters();
    Observable<StreamAction> getInstanceList(String clusterName);
}
