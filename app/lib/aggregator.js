var Utility = require('./utility.js')

var Aggregator = function() {
  var state = {
    source: null,
    aggregators: {},
    notifier: null
  }

  return {
    // Start listening to our event source.
    start: function(target) {
      var self = this

      state.source = new EventSource(target)
      state.source.addEventListener("message", function(e) { self.handleEvent(e) })
      state.notifier = setInterval(function() { self.notify() }, 1000)
    },

    // Stops listening to our event source, and stops dispatching events.
    stop: function() {
      if (state.source) {
        state.source.close()
      }

      if (state.notifier) {
        clearInterval(state.notifier)
      }
    },

    // Registers a new aggregator type.
    addAggregator: function(type, aggregator) {
      state.aggregators[type] = aggregator
    },

    // Handles an event from our event source, looking for an aggregator that can
    // handle it, and adding it to them.
    handleEvent: function(e) {
      var parsed = JSON.parse(e.data)

      var aggregator = state.aggregators[parsed.type]
      if(aggregator !== undefined) {
        aggregator.addValue(parsed)
      }
    },

    // Triggers an event to be dispatched for all aggregator types we have registered,
    // notifying listeners of all of the values they have seen so far.
    notify: function() {
      var aggregatorTypes = Object.keys(state.aggregators)
      for(var i = 0; i < aggregatorTypes.length; i++) {
        var aggregator = state.aggregators[aggregatorTypes[i]]
        this.dispatch(aggregator.getType(), aggregator.getValues())
      }
    },

    // Dispatches a global event containing the known items for the given type.
    dispatch: function(type, o) {
      var e = new CustomEvent("oscilloscope-data", {detail: {type: type, children: o}})
      window.dispatchEvent(e)
    }
  }
}

module.exports = Aggregator
