package com.bluestatedigital.oscilloscope.turbine;

import com.netflix.turbine.discovery.StreamDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamDiscoveryFactory {
    public static final String HOSTNAME = "{HOSTNAME}";
    public static final String PORT = "{PORT}";

    private static final Logger logger = LoggerFactory.getLogger(StreamDiscoveryFactory.class);

    private final Class<?> discoveryClass;
    private final String uriTemplate;

    public StreamDiscoveryFactory(String discoveryImpl, String uriTemplate) {
        try {
            this.discoveryClass = Class.forName(discoveryImpl);
        } catch(Exception e) {
            throw new RuntimeException("failed to find/load discovery implementation", e);
        }

        this.uriTemplate = uriTemplate;
    }

    public StreamDiscovery getDiscoveryForCluster(String clusterName) {
        try {
            return (StreamDiscovery) this.discoveryClass.getDeclaredConstructor(String.class, String.class).newInstance(clusterName, this.uriTemplate);
        } catch (Exception e) {
            logger.error("failed to instanciate stream discovery implementation", e);
            throw new RuntimeException(e);
        }
    }
}
