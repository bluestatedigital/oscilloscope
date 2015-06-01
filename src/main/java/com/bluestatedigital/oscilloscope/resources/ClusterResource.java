package com.bluestatedigital.oscilloscope.resources;

import com.bluestatedigital.oscilloscope.turbine.ClusterDiscoveryFactory;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/clusters")
@Produces(MediaType.APPLICATION_JSON)
public class ClusterResource
{
    private static final Logger logger = LoggerFactory.getLogger(ClusterResource.class);

    private final ClusterDiscoveryFactory discoveryFactory;

    public ClusterResource(ClusterDiscoveryFactory discoveryFactory) {

        this.discoveryFactory = discoveryFactory;
    }

    @GET
    @Timed
    public Collection<String> getClusters()
    {
        return this.discoveryFactory.getDiscoveryForCluster().getClusters();
    }
}