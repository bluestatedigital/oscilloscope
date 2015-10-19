var React = require('react')
var ReactDOM = require('react-dom')
var d3 = require('d3')
var _ = require('underscore')

var xAxisForCircle = "40%"
var yAxisForCircle = "40%"
var minRadiusForCircle = 5
var maxRadiusForCircle = 125

var circuitCircleRadius = d3.scale.pow().exponent(0.5).domain([0, 400]).rangeRound([minRadiusForCircle, maxRadiusForCircle])
var circuitColorRange = d3.scale.linear().domain([0, 25, 40, 50]).range(["#AFDBAF", "#FFCC00", "#FF9900", "red"])

var RequestGraph = React.createClass({
    getInitialState: function() {
        return {cx: xAxisForCircle, cy: yAxisForCircle, r: "5", fill: "#AFDBAF", data: [], sparkline: ""}
    },
    componentWillReceiveProps: function(newProps) {
        if(this.props.rateLine) {
            // Add the latest request count to our line.
            var currentTimeMs = new Date().getTime()
            var data = this.state.data
            data.push({"v": parseFloat(newProps.data.ratePerSecond), "t": currentTimeMs})

            while (data.length > 200) {
                data.shift()
            }

            if (data.length > 1 && data[0].v == 0 && data[1].v != 0) {
                data.shift()
            }

            var currentTimeMs = new Date().getTime()
            var xScale = d3.time.scale().domain([new Date(currentTimeMs - (60 * 1000 * 2)), new Date(currentTimeMs)]).range([0, 140])

            var yMin = d3.min(this.state.data, function (d) {
                return d.v;
            })
            var yMax = d3.max(this.state.data, function (d) {
                return d.v;
            })
            var yScale = d3.scale.linear().domain([yMin, yMax]).nice().range([96, 32])

            var sparkline = d3.svg.line()
                .x(function (d) {
                    return xScale(new Date(d.t))
                })
                .y(function (d) {
                    return yScale(d.v)
                })
                .interpolate("basis")

            this.setState({data: data, sparkline: sparkline(data)})
        }

        // Recalculate the radius for our request volume bubble.
        var newRadiusForCircle = circuitCircleRadius(newProps.data.ratePerSecondPerHost)
        if(parseInt(newRadiusForCircle) > parseInt(maxRadiusForCircle)) {
            newRadiusForCircle = maxRadiusForCircle
        }

        this.animate("r", newRadiusForCircle)
    },
    animate: function(attr, targetValue, duration, ease) {
        var cmp = this

        var interpolator = d3.interpolate(this.state[attr], targetValue)

        return d3.select(ReactDOM.findDOMNode(cmp))
            .transition()
            .duration(duration || 300)
            .ease(ease || "cubic-in-out")
            .tween(attr, function() {
                return function(t) {
                    cmp.setState(_.object([attr], [interpolator(t)]))
                }
            })
    },
    render: function() {
        return (
            <svg className="background">
                <circle key={"request_volume_" + this.props.data.name} cx={this.state.cx} cy={this.state.cy} r={this.state.r} style={{fill: this.state.fill, opacity: 0.75}}></circle>
                <path key={"request_rate_historical_" + this.props.data.name} d={this.state.sparkline}></path>
            </svg>
        )
    }
})

module.exports = RequestGraph
