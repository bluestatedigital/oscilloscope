package com.bluestatedigital.oscilloscope;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class OscilloscopeConfiguration extends Configuration
{
    @NotNull
    @JsonProperty
    private Map<String, Object> turbine;

    public Map<String, Object> getTurbineConfig()
    {
        return turbine;
    }
}
