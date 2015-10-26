var React = require('react')
var Configuration = require('../lib/configuration.js')
var Constants = require('../lib/constants.js')
var Aggregator = require('../lib/aggregator.js')
var HystrixCommandAggregator = require('../lib/aggregators/HystrixCommandAggregator.js')
var HystrixThreadPoolAggregator = require('../lib/aggregators/HystrixThreadPoolAggregator.js')
var Grouping = require('./Grouping.jsx')

var Monitoring = React.createClass({
  getInitialState: function() {
    var aggregator = new Aggregator()
    var commandAggregator = new HystrixCommandAggregator()
    var threadPoolAggregator = new HystrixThreadPoolAggregator()

    aggregator.addAggregator(Constants.HystrixCommandType, commandAggregator)
    aggregator.addAggregator(Constants.HystrixThreadPoolType, threadPoolAggregator)

    var subaggregators = {}
    subaggregators[Constants.HystrixCommandType] = commandAggregator
    subaggregators[Constants.HystrixThreadPoolType] = threadPoolAggregator

    return {
      aggregator: aggregator,
      subaggregators: subaggregators
    }
  },
  getMode: function() {
    if(this.state.streamMode == 'host') {
      return "Stream: " + this.state.streamTarget
    }

    return 'Cluster: ' + this.state.streamTarget
  },
  componentWillMount: function() {
    var decodedStreamData = JSON.parse(window.atob(this.props.params.data))
    var streamEndpoint = Configuration.getStreamEndpoint(decodedStreamData)
    this.state.aggregator.start(streamEndpoint)

    var streamMode = decodedStreamData.sm

    // 'h' is host target.  If that's empty, it means we're in cluster mode,
    // then we look to 'c.n' which is the name of the cluster.
    var streamTarget = decodedStreamData.h
    if(streamTarget == '') {
      streamTarget = decodedStreamData.c.n
    }

    this.setState({ streamMode: streamMode, streamTarget: streamTarget })
  },
  componentWillUnmount: function() {
    this.state.aggregator.stop()
  },
  render: function() {
    var commandType = Constants.HystrixCommandType
    var threadPoolType = Constants.HystrixThreadPoolType

    var commandAggregator = this.state.subaggregators[commandType]
    var threadPoolAggregator = this.state.subaggregators[threadPoolType]

    var commandConfig = Configuration.getTypeConfig(commandType)
    var threadPoolConfig = Configuration.getTypeConfig(threadPoolType)

    return (
      <div>
        <div className="row full-width">
          <Grouping type={commandType} aggregator={commandAggregator} options={commandConfig} />
        </div>

        <div className="row full-width">
          <Grouping type={threadPoolType} aggregator={threadPoolAggregator} options={threadPoolConfig} />
        </div>
      </div>
    )
  }
})

module.exports = Monitoring
