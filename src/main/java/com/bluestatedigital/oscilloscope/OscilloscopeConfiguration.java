package com.bluestatedigital.oscilloscope;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class OscilloscopeConfiguration extends Configuration
{
    @NotEmpty
    @JsonProperty
    private String streamDiscoveryClass = "com.bluestatedigital.oscilloscope.turbine.ConsulStreamDiscovery";

    @NotEmpty
    @JsonProperty
    private String clusterDiscoveryClass = "com.bluestatedigital.oscilloscope.turbine.ConsulClusterDiscovery";

    @NotEmpty
    @JsonProperty
    private String uriTemplate = "http://{HOSTNAME}:{PORT}/hystrix.stream";

    public String getStreamDiscoveryClass() {
        return this.streamDiscoveryClass;
    }

    public String getClusterDiscoveryClass() { return this.clusterDiscoveryClass; }

    public String getUriTemplate() {
        return this.uriTemplate;
    }
}
