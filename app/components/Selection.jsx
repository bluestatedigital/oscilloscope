var React = require('react')
var Select = require('react-select')
var History = require('react-router').History

require('react-select/dist/react-select.css')

var Selection = React.createClass({
  mixins: [ History ],
  getInitialState: function() {
    return {hostTarget: '', clusterTarget: '', clusters: []}
  },
  onHostSelectionChanged: function() {
    this.setState({ hostTarget: this.refs.host.value })
  },
  onClusterSelectionChanged: function(clusterName) {
    this.setState({ clusterTarget: clusterName })
  },
  getClusterValue: function() {
    return this.state.clusterTarget
  },
  shouldDisableSubmit: function() {
    return this.state.hostTarget == '' && this.state.clusterTarget == ''
  },
  onSubmit: function() {
    var clusterComponents = this.state.clusterTarget.split('/')
    var clusterName = clusterComponents[0]
    var clusterProvider = clusterComponents[1]

    // Route to our monitoring endpoint depending on whether we're in host
    // or cluster mode.  This shit is pretty ugly, but it's what we have to
    // do if we want to use a button instead of a normal anchor tag.
    var streamMode = 'host'
    var clusterData = {}
    if(this.state.hostTarget == '') {
      streamMode = 'cluster'
      clusterData = { n: clusterName, p: clusterProvider }
    }

    var streamData = {sm: streamMode, h: this.state.hostTarget, c: clusterData}
    var streamDataEncoded = window.btoa(JSON.stringify(streamData))

    this.history.pushState(null, `/monitor/${streamMode}/${streamDataEncoded}`, null)
  },
  componentDidMount: function() {
    $.get("/service/clusters", null, function(data) {
      var clusters = data.map(function(v, i) {
        return {'value': v.ClusterName + '/' + v.ClusterProvider, 'label': v.ClusterName + ' (' + v.ClusterProvider + ')'}
      })

      this.setState({clusters: clusters})
    }.bind(this))
  },
  render: function() {
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
                <Select name="cluster" ref="cluster" options={this.state.clusters} placeholder="Select a cluster..." value={this.getClusterValue()} onChange={this.onClusterSelectionChanged} />
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
              <label>Custom Host Target
                <input name="host" ref="host" type="text" placeholder="e.g. http://hystrix-host:7979/hystrix.stream" onChange={this.onHostSelectionChanged} />
              </label>
            </div>
          </div>

          <div className="row"><br /><br /></div>

          <div className="row">
            <div className="columns small-12 medium-10 medium-offset-1 large-10 large-offset-1">
              <button id="monitor" type="button" className="expand" onClick={this.onSubmit} disabled={this.shouldDisableSubmit()}>Monitor Cluster / Host</button>
            </div>
          </div>
        </form>
      </div>
    )
  }
})

module.exports = Selection
