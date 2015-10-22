package com.nuclearfurnace.oscilloscope.resources;

import com.nuclearfurnace.oscilloscope.discovery.Cluster;
import com.nuclearfurnace.oscilloscope.discovery.DiscoveryManager;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/clusters")
@Produces(MediaType.APPLICATION_JSON)
public class ClusterResource
{
    private final DiscoveryManager discoveryManager;

    public ClusterResource(DiscoveryManager discoveryManager) {
        this.discoveryManager = discoveryManager;
    }

    @GET
    @Timed
    public List<Cluster> getClusters() {
        return this.discoveryManager.getClusters();
    }
}
