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
        }

        return null
    },
    getSort: function() {
        if(this.props.options.sort) {
            var options = this.props.options.sort.map(function(o) {
                return <dd key={o[0]}><a href="#" data-sort-key={o[0]} onClick={this.handleSort}>{o[1]}</a></dd>
            }.bind(this))

            return (
                <dl className="sub-nav sort">
                    <dt>Sort: </dt>
                    {options}
                </dl>
            )
        }

        return null
    },
    handleDataEvent: function(e) {
        if(e.detail.type == this.props.options.eventType) {
            this.setState({data: e.detail.children})
        }
    },
    handleSort: function(e) {
      e.preventDefault()
      var sortKey = $(e.target).attr('data-sort-key')

      console.log("sort requested: " + sortKey)
    },
    componentDidMount: function() {
        window.addEventListener("oscilloscope-data", this.handleDataEvent)
    },
    componentWillUnmount: function() {
        window.removeEventListener("oscilloscope-data", this.handleDataEvent)
    },
    render: function() {
        return (
            <div className="group">
                <h3>{this.props.options.displayName}</h3>
                <hr />

                {this.getLegend()}
                {this.getSort()}

                <GroupingContainer type={this.props.type} data={this.state.data} children={[]} />
            </div>
        )
    }
})

module.exports = Grouping
