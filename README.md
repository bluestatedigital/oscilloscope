# oscilloscope
A Hystrix dashboard with spice.

# what it's trying to be
A better dashboard for monitoring Hystrix data, namely:

- better usability (should be equal on all devices, ideally)
- better styling (this is highly subjective, but remains a goal none the less)
- faster to start monitoring (this comes from auto-discovery of clusters to monitor)

The biggest thing that drove us to want to make this is: I don't want to have to figure out how to connect, and I don't want to spend much time getting things configured.

We aren't particularily familiar with Java, and while running a Tomcat container isn't rocket science, it's much simpler to simply run a fat JAR.  Give our fledging familiarity, we decided to use Dropwizard to provide the all-in-one of serving static assets and also providing the cluster discovery mechanism. (more on that below)

Almost more importantly than the ease of getting the dashboard running is using it.  The standard `hystrix-dashboard` project requires manual input - boooo!  With the use of Turbine to aggregate streams, you're already specifying clusters, or discovering clusters, that can be monitored.  We wanted to take advantage of that.  The result is a simpler AJAX endpoint, and select element, to choose from a list of discovered clusters.  Now it's as simple as configuring the application to use the right discovery mechanism for Turbine, and you reap the benefits in actually selecting which of those clusters to monitor.

Right now, this is hard-coded to use a Consul-based discovery mechanism, but we're working on having it use the same instance discovery implementation that Turbine gets initialized with.

# Super Important Legal Note Kinda Sort
We're using code from Hystrix itself (namely the dashboard UI assets to actually draw the command/thread pool metrics and graphs) but potentially other stuff.  We/I have every intent to license things properly, but we may be missing the correct licensing at times.  Please do not hesitate to file an issue if there's a licensing problem.  Open source code is a godsend, but we all want to make sure code is being used rightfully and legally.

You can reach out specifically to __toby@bluestatedigital.com__ about any licensing issues.
