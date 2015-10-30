var Constants = require('./constants.js')

var typeConfigs = {}
typeConfigs[Constants.HystrixCommandType] = {
  displayName: "Circuit Breakers",
  eventType: "HystrixCommand",
  legend: [
    ["success", "Success"],
    ["short_circuited", "Short-Circuited"],
    ["bad_request", "Bad Request"],
    ["timeout", "Timeout"],
    ["rejected", "Rejected"],
    ["failure", "Failure"],
    ["error_percentage", "Error %"]
  ],
  sort: [
    ["errorPercentage", "Error %"],
    ["name", "Alphabetical"],
    ["ratePerSecond", "Volume"]
  ]
}
typeConfigs[Constants.HystrixThreadPoolType] = {
  displayName: "Thread Pools",
  eventType: "HystrixThreadPool"
}

var Configuration = {
  getTypeConfig: function(type) {
    return typeConfigs[type]
  },
  getStreamEndpoint: function(params) {
    if(params.sm == 'host') {
      return '/service/stream/host?target=' + params.h
    }

    return '/service/stream/cluster?cluster=' + params.c.n + '&provider=' + params.c.p
  },
  getModeDisplayString: function(data) {
    var decodedParams = JSON.parse(window.atob(data))

    if(decodedParams.sm == 'host') {
      return 'Stream: ' + decodedParams.h
    }

    return 'Cluster: ' + decodedParams.c.n
  }
}

module.exports = Configuration
