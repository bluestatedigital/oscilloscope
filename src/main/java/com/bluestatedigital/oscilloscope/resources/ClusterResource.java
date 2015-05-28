package com.bluestatedigital.oscilloscope.resources;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
        return new ArrayList<>();
    }
}