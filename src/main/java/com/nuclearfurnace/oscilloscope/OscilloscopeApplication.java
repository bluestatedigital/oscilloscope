package com.nuclearfurnace.oscilloscope;

import com.nuclearfurnace.oscilloscope.resources.ClusterResource;
import com.nuclearfurnace.oscilloscope.resources.StreamResource;
import com.nuclearfurnace.oscilloscope.turbine.ClusterDiscoveryFactory;
import com.nuclearfurnace.oscilloscope.turbine.StreamDiscoveryFactory;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class OscilloscopeApplication extends Application<OscilloscopeConfiguration>
{
    public static void main(String[] args) throws Exception
    {
        new OscilloscopeApplication().run(args);
    }

    @Override
    public String getName()
    {
        return "oscilloscope";
    }

    @Override
    public void initialize(Bootstrap<OscilloscopeConfiguration> bootstrap)
    {
        // Configure our static assets to be served on the root.
        bootstrap.addBundle(new AssetsBundle("/app/", "/", "index.html"));
    }

    @Override
    public void run(OscilloscopeConfiguration configuration, Environment environment)
    {
        // Set up our cluster endpoint for getting cluster information.
        final ClusterDiscoveryFactory clusterDiscoveryFactory = new ClusterDiscoveryFactory(configuration.getClusterDiscoveryClass());
        final ClusterResource clusterResource = new ClusterResource(clusterDiscoveryFactory);
        environment.jersey().register(clusterResource);

        // Set up our stream endpoint for proxying single streams or aggregating cluster-wide streams.
        final StreamDiscoveryFactory streamDiscoveryFactory = new StreamDiscoveryFactory(configuration.getStreamDiscoveryClass(), configuration.getUriTemplate());
        final StreamResource streamResource = new StreamResource(streamDiscoveryFactory);
        environment.jersey().register(streamResource);
    }
}
