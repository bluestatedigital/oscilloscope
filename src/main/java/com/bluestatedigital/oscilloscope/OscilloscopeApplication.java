package com.bluestatedigital.oscilloscope;

import com.bluestatedigital.oscilloscope.resources.ClusterResource;
import com.bluestatedigital.oscilloscope.resources.StreamResource;
import com.bluestatedigital.oscilloscope.turbine.StreamDiscoveryFactory;
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
        final ClusterResource clusterResource = new ClusterResource();
        environment.jersey().register(clusterResource);

        // Set up our stream endpoint for proxying single streams or aggregating cluster-wide streams.
        final StreamDiscoveryFactory discoveryFactory = new StreamDiscoveryFactory(configuration.getDiscoveryClass());
        final StreamResource streamResource = new StreamResource(discoveryFactory);
        environment.jersey().register(streamResource);
    }
}
