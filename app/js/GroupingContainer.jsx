var React = require('react')
var MonitorPanel = require('./MonitorPanel.jsx')

var GroupingContainer = React.createClass({
    getInitialState: function() {
        return {children: this.props.data}
    },
    getChildren: function() {
        if(this.state.children.length == 0) {
            return this.getDefault()
        } else {
            var self = this
            return this.state.children.map(function(o) {
                // map to a generic display component that renders itself based on the type?
                return <MonitorPanel key={o.name} type={self.props.type} data={o} />
            })
        }
    },
    getDefault: function() {
        return (
            <div className="empty">
                <p>No data available!</p>
            </div>
        )
    },
    componentWillReceiveProps: function(nextProps) {
        this.setState({children: nextProps.data})
    },
    render: function () {
        return (
            <div className="children">
                {this.getChildren()}
            </div>
        )
    }
})

module.exports = GroupingContainer