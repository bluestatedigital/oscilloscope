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
  getStreamEndpoint: function(params) {
    if(params.sm == 'host') {
      return '/service/stream/host?target=' + params.h
    }

    return '/service/stream/cluster?cluster=' + params.c.n + '&provider=' + params.c.p
  }
}

module.exports = Configuration
