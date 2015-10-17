var React = require('react')
var Select = require('react-select')

require('react-select/dist/default.css')

var Selection = React.createClass({
  getInitialState: function() {
    return {streamTarget: '', clusterTarget: ''}
  },
  onStreamSelectionChanged: function() {
    this.setState({ streamTarget: this.refs.stream.value })
  },
  onClusterSelectionChanged: function(clusterName) {
    this.setState({ clusterTarget: clusterName })
  },
  getClusterValue: function() {
    return this.state.clusterTarget
  },
  shouldDisableSubmit: function() {
    return this.state.streamTarget == '' && this.state.clusterTarget == ''
  },
  onSubmit: function() {
    var clusterComponents = this.state.clusterTarget.split('/')
    var clusterName = clusterComponents[0]
    var clusterProvider = clusterComponents[1]

    this.props.switchToMonitoring({
      streamTarget: this.state.streamTarget,
      clusterName: clusterName,
      clusterProvider: clusterProvider
    })
  },
  render: function() {
    var clusters = [
      { value: 'guestlist-prod/static', label: 'guestlist-prod (static)' }
    ]

    return (
      <div>
        <div className="row full-width header text-center">
          <h1>Oscilloscope</h1>
          <h4 className="subheader">A Hystrix dashboard with spice.</h4>
        </div>

        <div className="row"><br /><br /></div>

        <div className="row">
          <div className="columns small-12 medium-10 medium-offset-1 large-10 large-offset-1">
            <p>Choose a <strong>Hystrix/Turbine</strong> target from the dropdown list, or supply a specific endpoint.</p>
          </div>
        </div>

        <form>
          <div className="row">
            <div className="columns small-12 medium-10 medium-offset-1 large-10 large-offset-1">
              <label>Available Clusters
                <Select name="cluster" ref="cluster" options={clusters} placeholder="Select a cluster..." value={this.getClusterValue()} onChange={this.onClusterSelectionChanged} />
              </label>
            </div>
          </div>

          <div className="row">
            <div className="columns small-12 medium-10 medium-offset-1 large-10 large-offset-1">
              <div className="stream-divider">
                <hr />
                <div className="stream-divider-msg">OR</div>
              </div>
            </div>
          </div>

          <div className="row">
            <div className="columns small-12 medium-10 medium-offset-1 large-10 large-offset-1">
              <label>Custom Stream Target
                <input name="stream" ref="stream" type="text" placeholder="e.g. http://hystrix-host:7979/hystrix.stream" onChange={this.onStreamSelectionChanged} />
              </label>
            </div>
          </div>

          <div className="row"><br /><br /></div>

          <div className="row">
            <div className="columns small-12 medium-10 medium-offset-1 large-10 large-offset-1">
              <button id="monitor" type="button" className="expand" onClick={this.onSubmit} disabled={this.shouldDisableSubmit()}>Monitor Cluster / Target</button>
            </div>
          </div>
        </form>
      </div>
    )
  }
})

module.exports = Selection
