var React = require('react')

var RequestGraph = React.createClass({
    render: function() {
        return (
            <svg className="background">
                <circle cx="50%" cy="50%" r="5" style={{fill: "rgb(0, 77, 0)"}}></circle>
            </svg>
        )
    }
})

module.exports = RequestGraph