var CircuitBreakerLegendOptions = [
  ["success", "Success"],
  ["short_circuited", "Short-Circuited"],
  ["bad_request", "Bad Request"],
  ["timeout", "Timeout"],
  ["rejected", "Rejected"],
  ["failure", "Failure"],
  ["error_percentage", "Error %"]
]

var Configuration = {
  "circuitBreakers": {
    displayName: "Circuit Breakers",
    eventType: "circuitBreakers",
    legend: CircuitBreakerLegendOptions
  },
  "threadPools": {
    displayName: "Thread Pools",
    eventType: "threadPools"
  },
  getStreamEndpoint: function(options) {
    if(options.streamTarget != '') {
      return '/service/stream/host?target=' + options.streamTarget
    }

    return '/service/stream/cluster?cluster=' + options.clusterName + '&provider=' + options.clusterProvider
  }
}

module.exports = Configuration
