package com.bluestatedigital.oscilloscope.turbine;

import com.netflix.turbine.discovery.StreamDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterDiscoveryFactory {
    private static final Logger logger = LoggerFactory.getLogger(ClusterDiscoveryFactory.class);

    private final Class<?> discoveryClass;

    public ClusterDiscoveryFactory(String discoveryImpl) {
        try {
            this.discoveryClass = Class.forName(discoveryImpl);
        } catch(Exception e) {
            throw new RuntimeException("failed to find/load cluster discovery implementation", e);
        }
    }

    public ClusterDiscovery getDiscoveryForCluster() {
        try {
            return (ClusterDiscovery) this.discoveryClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.error("failed to instanciate cluster discovery implementation", e);
            throw new RuntimeException(e);
        }
    }
}
