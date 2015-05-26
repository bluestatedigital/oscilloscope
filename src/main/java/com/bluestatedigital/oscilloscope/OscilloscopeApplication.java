package com.bluestatedigital.oscilloscope;

import com.bluestatedigital.oscilloscope.resources.ClusterResource;
import com.bluestatedigital.oscilloscope.stream.ProxyStreamServlet;
import com.netflix.config.ConfigurationManager;
import com.netflix.turbine.init.TurbineInit;
import com.netflix.turbine.streaming.servlet.TurbineStreamServlet;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.commons.configuration.MapConfiguration;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.HashMap;

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
        // Configure Turbine/Archaius to use our custom, Consul-based instance discovery implementation.  This finds all services
        // that have been tagged 'hystrix' (so they expose a Hystrix event stream) and creates clusters based on the service name.
        // Also leave the port out of the URL suffix, because that will be appended directly to the instance hostname.
        MapConfiguration oscilloscopeTurbineConfiguration = new MapConfiguration(new HashMap<String, Object>() {{
            put("InstanceDiscovery.impl", "com.bluestatedigital.oscilloscope.discovery.ConsulBasedInstanceDiscovery");
            put("turbine.instanceUrlSuffix", "turbine.instanceUrlSuffix=/hystrix.stream");
        }});

        ConfigurationManager.install(oscilloscopeTurbineConfiguration);

        // Configure our static assets to be served on the root.
        bootstrap.addBundle(new AssetsBundle("/app/", "/", "index.html"));
    }

    @Override
    public void run(OscilloscopeConfiguration configuration, Environment environment)
    {
        // Start Turbine.  Wahooo!
        //TurbineInit.init();

        // Map the 'turbine.stream' endpoint to the Turbine event stream servlet, and the 'proxy.stream' endpoint to
        // the proxying servlet.  The proxy servlet lets us get around CORS issues with EventSource.
        //environment.getApplicationContext().addServlet(new ServletHolder(new TurbineStreamServlet()), "/turbine.stream");
        environment.getApplicationContext().addServlet(new ServletHolder(new ProxyStreamServlet()), "/proxy.stream");

        // Now set up our application's own special endpoints.
        final ClusterResource clusterResource = new ClusterResource();
        environment.jersey().register(clusterResource);
    }
}
