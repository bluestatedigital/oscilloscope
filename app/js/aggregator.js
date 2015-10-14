var Utility = require('./utility.js')

var Aggregator = {
    source: null,
    commands: {},
    pools: {},
    notifier: null,
    start: function (target) {
        var self = this
        self.source = new EventSource(target)
        self.source.addEventListener("message", function(e) { self.handleEvent(e) })
        self.notifier = setInterval(function() { self.notify() }, 1000)
    },
    stop: function () {
        var self = this
        if (self.source) {
            self.source.close()
        }

        if (self.notifier) {
            clearInterval(self.notifier)
        }
    },
    handleEvent: function (e) {
        var self = this
        var parsed = JSON.parse(e.data)
        if (parsed.type == "HystrixCommand") {
            // handle as command
            self.commands[parsed.name] = self.preprocessCommand(parsed)
        }

        if (parsed.type == "HystrixThreadPool") {
            // handle as thread pool
            self.pools[parsed.name] = self.preprocessPool(parsed)
        }
    },
    notify: function () {
        var self = this
        var commands = self.commands
        var pools = self.pools

        var commandData = Object.keys(commands).map(function (k) { return commands[k] })
        var poolData = Object.keys(pools).map(function (k) { return pools[k] })

        self.dispatch("circuitBreakers", commandData)
        self.dispatch("threadPools", poolData)
    },
    dispatch: function (type, o) {
        var e = new CustomEvent("oscilloscope-data", {detail: {type: type, children: o}})
        window.dispatchEvent(e)
    },
    preprocessCommand: function(c) {
        var numberSeconds = c.propertyValue_metricsRollingStatisticalWindowInMilliseconds / 1000

        var totalRequests = c.requestCount
        if (totalRequests < 0) {
            totalRequests = 0
        }
        var ratePerSecond =  Utility.getRoundedNumber(totalRequests / numberSeconds)
        var ratePerSecondPerHost =  Utility.getRoundedNumber(totalRequests / numberSeconds / c.reportingHosts)

        c.ratePerSecond = ratePerSecond
        c.ratePerSecondPerHost = ratePerSecondPerHost

        return c
    },
    preprocessPool: function(p) {
        var numberSeconds = p.propertyValue_metricsRollingStatisticalWindowInMilliseconds / 1000;

        var totalThreadsExecuted = p.rollingCountThreadsExecuted
        if (totalThreadsExecuted < 0) {
            totalThreadsExecuted = 0
        }

        var ratePerSecond =  Utility.getRoundedNumber(totalThreadsExecuted / numberSeconds)
        var ratePerSecondPerHost =  Utility.getRoundedNumber(totalThreadsExecuted / numberSeconds / p.reportingHosts)

        p.ratePerSecond = ratePerSecond
        p.ratePerSecondPerHost = ratePerSecondPerHost

        return p
    }
}

module.exports = Aggregator
