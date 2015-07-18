var CircuitBreakerSortOptions = [
    ["errorThenVolume", "Error Then Volume"],
    ["alphabetical", "Alphabetical"],
    ["volume", "Volume"],
    ["error", "Error"],
    ["mean", "Mean"],
    ["median", "Median"],
    ["90th", "90th"],
    ["99th", "99th"],
    ["999th", "99.9th"]
]

var CircuitBreakerLegendOptions = [
    ["success", "Success"],
    ["short_circuited", "Short-Circuited"],
    ["bad_request", "Bad Request"],
    ["timeout", "Timeout"],
    ["rejected", "Rejected"],
    ["failure", "Failure"],
    ["error_percentage", "Error %"]
]

var ThreadPoolSortOptions = [
    ["alphabetical", "Alphabetical"],
    ["volume", "Volume"]
]

var Configuration = {
    "circuitBreakers": {
        displayName: "Circuit Breakers",
        eventType: "circuitBreakers",
        sort: CircuitBreakerSortOptions,
        legend: CircuitBreakerLegendOptions
    },
    "threadPools": {
        displayName: "Thread Pools",
        eventType: "threadPools",
        sort: ThreadPoolSortOptions
    },
    getStreamInfo: function() {
        var vars = []
        var hash = []

        var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&')
        for(var i = 0; i < hashes.length; i++)
        {
            hash = hashes[i].split('=')
            vars.push(hash[0])
            vars[hash[0]] = hash[1]
        }

        var stream = vars["stream"]
        var cluster = vars["cluster"]
        var proxy = vars["proxy"]

        var streamTarget = ""
        var streamType = "stream"
        var streamName = ""

        if (stream != undefined) {
            streamTarget = "/service/stream/host?target=" + stream
            streamName = stream
        } else if (cluster != undefined) {
            streamTarget = "/service/stream/cluster?cluster=" + cluster
            streamType = "cluster"
            streamName = cluster
        }

        if (proxy != undefined) {
            var proxiedTarget = proxy + streamTarget
            streamTarget = proxiedTarget
        }

        return {type: streamType, target: streamTarget, name: streamName}
    }
}

module.exports = Configuration