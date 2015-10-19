var React = require('react')
var GroupingContainer = require('./GroupingContainer.jsx')

var Grouping = React.createClass({
    getInitialState: function() {
        return {data: []}
    },
    getLegend: function() {
        if(this.props.options.legend) {
            var options = this.props.options.legend.map(function(o) {
                return <dd key={o[0]} className={"legend legend_" + o[0]}>{o[1]}</dd>
            })

            return (
                <dl className="sub-nav legend">
                    <dt>Legend: </dt>
                    {options}
                </dl>
            )
        } else {
            return <div className="legend" />
        }
    },
    handleDataEvent: function(e) {
        if(e.detail.type == this.props.options.eventType) {
            this.setState({data: e.detail.children})
        }
    },
    componentDidMount: function() {
        window.addEventListener("oscilloscope-data", this.handleDataEvent)
    },
    componentWillUnmount: function() {
        window.removeEventListener("oscilloscope-data", this.handleDataEvent)
    },
    render: function() {
        var legend = this.getLegend()

        return (
            <div className="group">
                <h3>{this.props.options.displayName}</h3>
                <hr />

                {legend}

                <GroupingContainer type={this.props.type} data={this.state.data} children={[]} />
            </div>
        )
    }
})

module.exports = Grouping
