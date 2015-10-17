var React = require('react')
var Selection = require('./Selection.jsx')
var Monitoring = require('./Monitoring.jsx')

var Application = React.createClass({
  getInitialState: function() {
    return {step: "select"}
  },
  switchToMonitoring: function(options) {
    this.setState({step: "monitor", monitorOptions: options})
  },
  switchToSelection: function() {
    this.setState({step: "select"})
  },
  render: function() {
    if(this.state.step == "select") {
      return <Selection switchToMonitoring={this.switchToMonitoring} />
    } else {
      return <Monitoring switchToSelection={this.switchToSelection} options={this.state.monitorOptions} />
    }
  }
})

module.exports = Application
