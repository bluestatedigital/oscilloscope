package com.bluestatedigital.oscilloscope.resources;

import com.bluestatedigital.oscilloscope.discovery.ConsulBasedInstanceDiscovery;
import com.codahale.metrics.annotation.Timed;
import com.netflix.turbine.discovery.Instance;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

@Path("/clusters")
@Produces(MediaType.APPLICATION_JSON)
public class ClusterResource
{
    @GET
    @Timed
    public Collection<String> getClusters() throws Exception
    {
        //ConsulBasedInstanceDiscovery discovery = new ConsulBasedInstanceDiscovery();
        //return discovery.getClusterList();
        ArrayList<String> clusters = new ArrayList<>();
        clusters.add("fake-svc-prod");
        clusters.add("fake-svc-test");

        return clusters;
    }
}