# oscilloscope
A Hystrix dashboard with spice.

# What/Why
Oscilloscope is a revamped `hystrix-dashboard`.  It aims to look better on non-desktop devices, it aims to be cleaner and more succinct, and it aims to provide a more integrated experience for exploring your individual Hystrix-enabled hosts as well as Hystrix-enabled clusters.

Instead of requiring users to run both `hystrix-dashboard` and `turbine` separately, Oscilloscope runs them all as one service.  Better still, Oscilloscope can be configured to use multiple sources for finding these Hystrix-enabled clusters.  The UI exposes this in a way that makes drilling into a cluster easy.

No fumbling with URLs by hand.  A variety of ways to find clusters.  Easier to operate.

# Getting Started
You can follow the [Quick Start](https://github.com/nuclearfurnace/oscilloscope/wiki/Quick-Start) guide to get going.

# Based On
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

# Super Important Legal Notice Stuff
We're using code from Hystrix and Turbine themselves, and also from other projects/products.  We/I have every intent to license things properly, but we may be missing the correct licensing at times.  Please do not hesitate to file an issue if there's a licensing problem.  Open source code is a godsend, but we all want to make sure code is being used rightfully and legally.

You can reach out specifically to __toby@nuclearfurnace.com__ about any licensing issues.

Other than that, all of the code that *is* ours uses the Apache License.  Take it, fork it, switch - upgrade it.  Go nuts.  This land is my land, this land is your land, etc etc.
