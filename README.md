# oscilloscope
A Hystrix dashboard with spice.

# what it's trying to be
A better dashboard for monitoring Hystrix data, namely:

- better usability (should be equal on all devices, ideally)
- better styling (this is highly subjective, but remains a goal none the less)
- faster to actually start using it (this comes from auto-discovery of clusters to monitor)

The biggest thing that drove us to want to make this is: I don't want to have to figure out how to connect, and I don't want to spend much time getting things configured.

We aren't particularily familiar with Java, and while running a Tomcat container isn't rocket science, it's much simpler to simply run a fat JAR.  Give our fledging familiarity, we decided to use Dropwizard to provide the all-in-one of serving static assets and also providing the cluster discovery mechanism. (more on that below)

Almost more importantly than the ease of getting the dashboard running is using it.  The standard `hystrix-dashboard` project requires manual input - boooo!  With the use of Turbine to aggregate streams, you're already specifying clusters, or discovering clusters, that can be monitored.  We wanted to take advantage of that. 

By adding an AJAX endpoint that lists out the available clusters, and tweaking the landing page UI to expose that list, you can now quickly and easily select a cluster to monitor. Now it's as simple as configuring the application to use the right discovery mechanism for Turbine, and you reap the benefits by being able to actually select which of those clusters to monitor right from Oscilloscope itself, no manually constructing URLs.

# Based On
- `dropwizard`: project skeleton/framework
- `hystrix-dashboard`: UI code
- `turbine`: cluster discovery, integrated Turbine endpoint
- `jquery`: JS framework of choice
- `d3`: circuit breaker / thread pool visualizations
- `foundation`: UI framework
- `chosen`: jQuery plugin for fancy select elements

# Super Important Legal Note Kinda Sorta
We're using code from Hystrix and Turbine themselves, and also from other projects/products.  We/I have every intent to license things properly, but we may be missing the correct licensing at times.  Please do not hesitate to file an issue if there's a licensing problem.  Open source code is a godsend, but we all want to make sure code is being used rightfully and legally.

You can reach out specifically to __toby@bluestatedigital.com__ about any licensing issues.

Other than that, all of the code that *is* ours uses the Apache License.  Take it, fork it, switch - upgrade it.  Go nuts.  This land is my land, this land is your land, etc etc.
