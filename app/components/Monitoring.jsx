var React = require('react')
var Configuration = require('../lib/configuration.js')
var Aggregator = require('../lib/aggregator.js')
var Grouping = require('./Grouping.jsx')

var Monitoring = React.createClass({
  getMode: function() {
    if(this.props.options.streamTarget != '') {
      return "Stream: " + this.props.options.streamTarget
    }

    return 'Cluster: ' + this.props.options.clusterName
  },
  componentWillMount: function() {
    var streamEndpoint = Configuration.getStreamEndpoint(this.props.options)
    Aggregator.start(streamEndpoint)
  },
  componentWillUnmount: function() {
    Aggregator.stop()
  },
  render: function() {
    return (
      <div>
        <div className="row full-width">
          <h1>Oscilloscope <small>{this.getMode()}</small></h1>
        </div>

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
