var React = require('react')
var RequestGraph = require('./RequestGraph.jsx')

var MonitorPanel = React.createClass({
    getRoundedNumber: function(num) {
        var dec = 1
        var result = Math.round(num*Math.pow(10,dec))/Math.pow(10,dec)
        var resultAsString = result.toString()
        if(resultAsString.indexOf('.') == -1) {
            resultAsString = resultAsString + '.0'
        }

        return resultAsString
    },
    getCircuitStatus: function() {
        if(this.props.data.propertyValue_circuitBreakerForceOpen) {
            return <span className="status-value status-force-open">Forced Open</span>
        }

        if(this.props.data.propertyValue_circuitBreakerForceClosed) {
            return <span className="status-value status-force-closed">Forced Closed</span>
        }

        // This value defaults to a boolean but legitimately gets used as an integer.
        // This is stupid, but whatever.
        var circuitStatus = this.props.data.isCircuitBreakerOpen
        if(circuitStatus == 0) {
            return <span className="status-value status-closed">Closed</span>
        }

        if(circuitStatus == this.props.data.reportingHosts) {
            return <span className="status-value status-open">Open</span>
        }

        return <span className="status-value status-partial-open">Open</span>
    },
    render: function() {
        var rejectedCount = this.props.data.rollingCountThreadPoolRejected
        if(this.props.data.propertyValue_executionIsolationStrategy == 'SEMAPHORE') {
            rejectedCount = this.props.data.rollingCountSemaphoreRejected
        }

        var numberSeconds = this.props.data.propertyValue_metricsRollingStatisticalWindowInMilliseconds / 1000;

        var totalRequests = this.props.data.requestCount;
        if (totalRequests < 0) {
            totalRequests = 0;
        }
        var ratePerSecond =  this.getRoundedNumber(totalRequests / numberSeconds);
        var ratePerSecondPerHost =  this.getRoundedNumber(totalRequests / numberSeconds / this.props.data.reportingHosts) ;

        return (
            <div className="monitor-panel">
                <RequestGraph />
                <div className="counter-overlay">
                    <p className="monitor-name text-right">{this.props.data.name}</p>
                    <div className="vertical-columns">
                        <div className="cell">
                            <span className="line line-large legend legend_error_percentage">0.0%</span>
                        </div>
                        <div className="cell separate">
                            <span className="line legend legend_timeout">{this.props.data.rollingCountTimeout}</span>
                            <span className="line legend legend_rejected">{rejectedCount}</span>
                            <span className="line legend legend_failure">{this.props.data.rollingCountFailure}</span>
                        </div>
                        <div className="cell separate">
                            <span className="line legend legend_success">{this.props.data.rollingCountSuccess}</span>
                            <span className="line legend legend_short_circuited">{this.props.data.rollingCountShortCircuited}</span>
                            <span className="line legend legend_bad_request">{this.props.data.rollingCountBadRequests}</span>
                        </div>
                        <br className="clear" />
                        <br />
                    </div>
                    <div className="rate">
                        <span className="rate-label">Host: </span>
                        <span className="rate-value">{ratePerSecondPerHost}/s</span>
                    </div>
                    <div className="rate">
                        <span className="rate-label">Cluster: </span>
                        <span className="rate-value">{ratePerSecond}/s</span>
                    </div>
                    <div className="status">
                        <span className="status-label">Circuit </span>
                        {this.getCircuitStatus()}
                    </div>

                    <br />

                    <div className="values-table">
                        <div className="values-row">
                            <div className="values-cell values-label">Hosts</div>
                            <div className="values-cell values-value">{this.props.data.reportingHosts}</div>
                            <div className="values-cell values-label">95th</div>
                            <div className="values-cell values-value">{this.props.data.latencyExecute['95']}ms</div>
                        </div>
                        <div className="values-row">
                            <div className="values-cell values-label">Median</div>
                            <div className="values-cell values-value">{this.props.data.latencyExecute['50']}ms</div>
                            <div className="values-cell values-label">99th</div>
                            <div className="values-cell values-value">{this.props.data.latencyExecute['99']}ms</div>
                        </div>
                        <div className="values-row">
                            <div className="values-cell values-label">Mean</div>
                            <div className="values-cell values-value">{this.props.data.latencyExecute_mean}ms</div>
                            <div className="values-cell values-label">99.5th</div>
                            <div className="values-cell values-value">{this.props.data.latencyExecute['99.5']}ms</div>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
})

module.exports = MonitorPanel