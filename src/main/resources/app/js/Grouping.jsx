var React = require('react')
var GroupingContainer = require('./GroupingContainer.jsx')

var Grouping = React.createClass({
    getInitialState: function() {
        return {data: []}
    },
    onSortHandler: function(e) {
        var sortType = $(e.target).data('sort-type')
        this.setState({sortType: sortType})
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
    render: function() {
        var that = this
        var sortOptions = this.props.options.sort.map(function(o) {
            return <dd><a data-sort-type={o[0]} key={o[0]} onClick={that.onSortHandler}>{o[1]}</a></dd>
        })
        var legend = this.getLegend()

        return (
            <div className="group">
                <h3>{this.props.options.displayName}</h3>

                <dl className="sub-nav">
                    <dt>Sort: </dt>
                    {sortOptions}
                </dl>

                {legend}

                <GroupingContainer type={this.props.type} data={this.state.data} children={[]} />
            </div>
        )
    },
    componentDidMount: function() {
        var that = this
        window.addEventListener("oscilloscope-data", function(e) {
            if(e.detail.type == that.props.options.eventType) {
                that.setState({data: e.detail.children})
            }
        })
    }
})

module.exports = Grouping