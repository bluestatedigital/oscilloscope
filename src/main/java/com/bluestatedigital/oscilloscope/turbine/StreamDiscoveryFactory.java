package com.bluestatedigital.oscilloscope.turbine;

import com.netflix.turbine.discovery.StreamDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamDiscoveryFactory {
    private static final Logger logger = LoggerFactory.getLogger(StreamDiscoveryFactory.class);

    private final Class<?> discoveryClass;

    public StreamDiscoveryFactory(String discoveryImpl) {
        try {
            this.discoveryClass = Class.forName(discoveryImpl);
        } catch(Exception e) {
            throw new RuntimeException("failed to load discovery implementation", e);
        }
    }

    public StreamDiscovery getDiscoveryForCluster(String clusterName) {
        try {
            return (StreamDiscovery) this.discoveryClass.getDeclaredConstructor(String.class).newInstance(clusterName);
        } catch (Exception e) {
            logger.error("could not load stream discovery implementation", e);
            throw new RuntimeException(e);
        }
    }
}
