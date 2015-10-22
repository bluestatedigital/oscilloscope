var Sorting = require('../sorting.js')
var Configuration = require('../configuration.js')
var Utility = require('../utility.js')

var HystrixCommandAggregator = function() {
  var state = {
    type: Configuration.HystrixCommandType,
    values: {},
    sorting: new Sorting()
  }

  return {
    // Adds a value to this aggregator.
    addValue: function(value) {
      if(value.type != state.type) {
        return
      }

      // Invalidate our sorting when we see a new command to force a timely resort.
      if(state.values[value.name] === undefined) {
        state.sorting.invalidate()
      }

      // Preprocess our value and store it.
      var processedValue = this.preprocess(value)
      state.values[value.name] = processedValue
    },

    // Sets the key to sort our values on.
    sortBy: function(key) {
      state.sorting.setSortKey(key)
    },

    // Gets the type string for this aggregator.
    getType: function() {
      return state.type
    },

    // Gets all of our current values, sorted.
    getValues: function() {
      var values = Object.keys(state.values).map(function (k) { return state.values[k] })

      // This returns a copy.  It is not the same as Array.sort().
      return state.sorting.sort(values)
    },

    // Preprocesses the value to add some values we don't get off the bat.
    preprocess: function(value) {
      var numberSeconds = value.statsWindowMs / 1000

      var totalRequests = value.requestCount
      if (totalRequests < 0) {
        totalRequests = 0
      }

      var ratePerSecond =  Utility.getRoundedNumber(totalRequests / numberSeconds)
      var ratePerSecondPerHost =  Utility.getRoundedNumber(totalRequests / numberSeconds / value.reportingHosts)

      value.ratePerSecond = ratePerSecond
      value.ratePerSecondPerHost = ratePerSecondPerHost

      return value
    }
  }
}

module.exports = HystrixCommandAggregator
