var React = require('react')
var d3 = require('d3')
var _ = require('underscore')

var maxXaxisForCircle="40%";
var maxYaxisForCircle="40%";
var maxRadiusForCircle="125";

var circuitCircleRadius = d3.scale.pow().exponent(0.5).domain([0, 400]).range(["5", maxRadiusForCircle]);
var circuitCircleYaxis = d3.scale.linear().domain([0, 400]).range(["30%", maxXaxisForCircle]);
var circuitCircleXaxis = d3.scale.linear().domain([0, 400]).range(["30%", maxYaxisForCircle]);
var circuitColorRange = d3.scale.linear().domain([0, 25, 40, 50]).range(["#AFDBAF", "#FFCC00", "#FF9900", "red"]);

var RequestGraph = React.createClass({
    getInitialState: function() {
        return {cx: "30%", cy: "30%", r: "5", fill: "#AFDBAF"}
    },
    componentWillReceiveProps: function(newProps) {
        var newXaxisForCircle = circuitCircleXaxis(newProps.data.ratePerSecondPerHost);
        if(parseInt(newXaxisForCircle) > parseInt(maxXaxisForCircle)) {
            newXaxisForCircle = maxXaxisForCircle;
        }
        var newYaxisForCircle = circuitCircleYaxis(newProps.data.ratePerSecondPerHost);
        if(parseInt(newYaxisForCircle) > parseInt(maxYaxisForCircle)) {
            newYaxisForCircle = maxYaxisForCircle;
        }
        var newRadiusForCircle = circuitCircleRadius(newProps.data.ratePerSecondPerHost);
        if(parseInt(newRadiusForCircle) > parseInt(maxRadiusForCircle)) {
            newRadiusForCircle = maxRadiusForCircle;
        }

        this.animate("cx", newXaxisForCircle)
        this.animate("cy", newYaxisForCircle)
        this.animate("r", newRadiusForCircle)
    },
    animate: function(attr, targetValue, duration, ease) {
        var cmp = this;

        var interpolator;
        if (_.isFunction(targetValue)) {
            interpolator = targetValue;
        } else {
            interpolator = d3.interpolate(this.state[attr], targetValue);
        }

        return d3.select(cmp.getDOMNode())
            .transition()
            .duration(duration || 500)
            .ease(ease || "cubic-in-out")
            .tween(attr, function() {
                return function(t) {
                    cmp.setState(_.object([attr], [interpolator(t)]));
                };
            });
    },
    render: function() {
        return (
            <svg className="background">
                <circle key={"request_volume_" + this.props.data.name} cx={this.state.cx} cy={this.state.cy} r={this.state.r} style={{fill: this.state.fill}}></circle>
            </svg>
        )
    }
})

module.exports = RequestGraph