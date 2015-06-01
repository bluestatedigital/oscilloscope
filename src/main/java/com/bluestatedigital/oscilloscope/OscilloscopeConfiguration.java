package com.bluestatedigital.oscilloscope;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class OscilloscopeConfiguration extends Configuration
{
    @NotNull
    @JsonProperty
    private String discoveryClass;

    public String getDiscoveryClass() {
        return this.discoveryClass;
    }
}
