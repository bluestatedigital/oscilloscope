var React = require('react')

var MonitorPanel = React.createClass({
    render: function() {
        return (
            <div className="monitor-panel">
                <div className="counter-overlay">
                    <p className="monitor-name text-right">{this.props.data.name}</p>
                    <div className="vertical-columns">
                        <div className="cell">
                            <span className="line line-large legend legend_error_percentage">0.0%</span>
                        </div>
                        <div className="cell separate">
                            <span className="line legend legend_timeout">{this.props.data.rollingCountTimeout}</span>
                            <span className="line legend legend_rejected">{this.props.data.rollingCountThreadPoolRejected}</span>
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
                        <span className="rate-value">0.0/s</span>
                    </div>
                    <div className="rate">
                        <span className="rate-label">Cluster: </span>
                        <span className="rate-value">0.0/s</span>
                    </div>
                    <div className="status">
                        <span className="status-label">Circuit </span>
                        <span className="status-value status-open">Open</span>
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