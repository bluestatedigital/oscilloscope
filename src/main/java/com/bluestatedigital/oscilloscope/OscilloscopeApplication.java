package com.bluestatedigital.oscilloscope;

import com.bluestatedigital.oscilloscope.resources.ClusterResource;
import com.bluestatedigital.oscilloscope.stream.ProxyStreamServlet;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlet.ServletHolder;

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
        // Set up our proxying servlet for one-to-one streams.
        environment.getApplicationContext().addServlet(new ServletHolder(new ProxyStreamServlet()), "/stream/proxy");

        // Set up our cluster endpoint for getting cluster information.
        final ClusterResource clusterResource = new ClusterResource();
        environment.jersey().register(clusterResource);
    }
}
