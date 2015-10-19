var React = require('react')
var Configuration = require('../lib/configuration.js')
var Aggregator = require('../lib/aggregator.js')
var Grouping = require('./Grouping.jsx')

var Monitoring = React.createClass({
  getMode: function() {
    if(this.state.streamMode == 'host') {
      return "Stream: " + this.state.streamTarget
    }

    return 'Cluster: ' + this.state.streamTarget
  },
  componentWillMount: function() {
    var decodedStreamData = JSON.parse(window.atob(this.props.params.data))
    var streamEndpoint = Configuration.getStreamEndpoint(decodedStreamData)
    Aggregator.start(streamEndpoint)

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
    Aggregator.stop()
  },
  render: function() {
    return (
      <div>
        <div className="row full-width">
          <Grouping type="circuitBreakers" options={Configuration["circuitBreakers"]} />
        </div>

        <div className="row full-width">
          <Grouping type="threadPools" options={Configuration["threadPools"]} />
        </div>
      </div>
    )
  }
})

module.exports = Monitoring
