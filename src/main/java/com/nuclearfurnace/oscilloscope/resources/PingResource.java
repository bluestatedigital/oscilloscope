package com.nuclearfurnace.oscilloscope.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/ping")
@Produces(MediaType.APPLICATION_JSON)
public class PingResource
{
    public PingResource() {
    }

    @GET
    public boolean getPing() {
        return true;
    }
}
