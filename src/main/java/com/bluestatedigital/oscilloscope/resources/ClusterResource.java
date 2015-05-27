package com.bluestatedigital.oscilloscope.resources;

import com.codahale.metrics.annotation.Timed;
import com.netflix.turbine.discovery.InstanceDiscovery;
import com.netflix.turbine.plugins.PluginsFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.stream.Collectors;

@Path("/clusters")
@Produces(MediaType.APPLICATION_JSON)
public class ClusterResource
{
    @GET
    @Timed
    public Collection<String> getClusters() throws Exception
    {
        InstanceDiscovery discovery = PluginsFactory.getInstanceDiscovery();

        return discovery.getInstanceList()
                .stream()
                .map(i -> i.getCluster())
                .distinct()
                .collect(Collectors.toList());
    }
}