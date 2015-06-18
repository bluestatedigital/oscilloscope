var React = require('react/addons')
var ReactCSSTransitionGroup = React.addons.CSSTransitionGroup
var Configuration = require('./configuration.js')
var Aggregator = require('./aggregator.js')
var Grouping = require('./Grouping.jsx')

var Application = React.createClass({
    getInitialState: function() {
        var info = Configuration.getStreamInfo()
        if(info.target == "") {
            return {error_state: "missing_target"}
        }

        return {streamType: info.type, streamTarget: info.target, streamName: info.name}
    },
    getMode: function() {
        var streamType = "Cluster"
        if(this.state.streamType == "stream") {
            streamType = "Stream"
        }

        return streamType + ': ' + this.state.streamName
    },
    componentWillMount: function() {
        if(this.state.streamTarget && this.state.streamTarget != "") {
            Aggregator.start(this.state.streamTarget)
        }
    },
    componentWillUnmount: function() {
        Aggregator.stop()
    },
    render: function() {
        if(this.state.error_state == "missing_target") {
            return (
                <div>
                    <div className="row full-width">
                        <ReactCSSTransitionGroup transitionName="missing-target" transitionAppear={true}>
                            <div key="error-message" className="text-center small-12 medium-6 small-centered columns">
                                <h2>Whoa, nelly! <small>We couldn't find a target to connect to.</small></h2>
                                <p>Did you click a link to get here?  It probably got cut off. <a href="/">Start over.</a></p>
                            </div>
                        </ReactCSSTransitionGroup>
                    </div>
                </div>
            )
        }

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

module.exports = Application