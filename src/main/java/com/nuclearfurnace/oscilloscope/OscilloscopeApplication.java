package com.nuclearfurnace.oscilloscope;

import com.nuclearfurnace.oscilloscope.discovery.DiscoveryManager;
import com.nuclearfurnace.oscilloscope.discovery.ProviderRegistry;
import com.nuclearfurnace.oscilloscope.discovery.providers.ConsulClusterProvider;
import com.nuclearfurnace.oscilloscope.discovery.providers.ElasticLoadBalancerClusterProvider;
import com.nuclearfurnace.oscilloscope.discovery.providers.StaticConfigurationClusterProvider;
import com.nuclearfurnace.oscilloscope.discovery.tasks.RefreshProvidersTask;
import com.nuclearfurnace.oscilloscope.resources.PingResource;
import com.nuclearfurnace.oscilloscope.resources.ClusterResource;
import com.nuclearfurnace.oscilloscope.resources.StreamResource;
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
        // Register our discovery providers.
        ProviderRegistry.registerProvider("static", new StaticConfigurationClusterProvider());
        ProviderRegistry.registerProvider("elb", new ElasticLoadBalancerClusterProvider());
        ProviderRegistry.registerProvider("consul", new ConsulClusterProvider());

        // Set up our discovery manager.
        DiscoveryManager discoveryManager = new DiscoveryManager();
        environment.lifecycle().manage(discoveryManager);

        RefreshProvidersTask refreshProvidersTask = new RefreshProvidersTask(discoveryManager);
        environment.admin().addTask(refreshProvidersTask);

        final PingResource pingResource = new PingResource();
        environment.jersey().register(pingResource);

        // Set up our cluster endpoint for getting cluster information.
        final ClusterResource clusterResource = new ClusterResource(discoveryManager);
        environment.jersey().register(clusterResource);

        final StreamResource streamResource = new StreamResource(discoveryManager);
        environment.jersey().register(streamResource);
    }
}
