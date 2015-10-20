# oscilloscope
A Hystrix dashboard with spice.

# what it's trying to be
A better dashboard for monitoring Hystrix data, namely:

- better usability (should be equal on all devices, ideally)
- better styling (this is highly subjective, but remains a goal none the less)
- faster to actually start using it (harnessing the power of service discovery)

# why it's trying to be that way
Fundamentally, `hystrix-dashboard` works, and works well.  Netflix's use case was rapid insight to the state of their circuit breakers, and it achieves that.

To me, the dashboard is a little spartan, and lacked a big feature that maybe just didn't quiteee make sense for Netflix, but makes a lot of sense to me: automatic cluster discovery.

You're going along, using Turbine to combine the event streams and then pointing the dashboard at your Turbine endpoint.  Maybe they need dedicated Turbine instances because the streams are so heavy. but to me, having the dashboard do that discovery and aggregation made a lot of sense to me.

So, Oscilloscope intends to give you a way to describe your service discovery mechanism -- Eureka, Consul, whatever -- and automatically find all of the "clusters" of machines that can have their Hystrix data aggregated and displayed.  Turbine did the aggregation already, we just needed a way to figure out all of the groups it was able to aggregate and let the user be able to pick one of those groups.

No special URL to craft by hand, or have to bookmark.  You can still give it a direct URL, whether it's a Hystrix stream or Turbine stream, but for viewing a cluster, it can (and is) much easier.

# getting started
You'll need to install Java, Maven, and node.js/io.js to build Oscilloscope.

    git clone https://github.com/nuclearfurnace/oscilloscope.git
    cd oscilloscope
    npm install
    webpack
    mvn package
    java -jar target/oscilloscope-0.0.1.jar server oscilloscope.yaml

# based on
- `dropwizard`: project skeleton/framework
- `hystrix-dashboard`: some UI code
- `turbine`: cluster discovery, integrated Turbine endpoint
- `jquery`: general JS framework of choice
- `d3`: circuit breaker / thread pool visualizations
- `react`: UI framework
- `foundation`: UI building blocks
- `react-select`: React component for fancy selects
- `react-router`: React component for SPA routing
- and others (`underscore`, `webpack`, `consul`, `archaius`, etc)

# super important legal notice sorta kinda
We're using code from Hystrix and Turbine themselves, and also from other projects/products.  We/I have every intent to license things properly, but we may be missing the correct licensing at times.  Please do not hesitate to file an issue if there's a licensing problem.  Open source code is a godsend, but we all want to make sure code is being used rightfully and legally.

You can reach out specifically to __toby@nuclearfurnace.com__ about any licensing issues.

Other than that, all of the code that *is* ours uses the Apache License.  Take it, fork it, switch - upgrade it.  Go nuts.  This land is my land, this land is your land, etc etc.
