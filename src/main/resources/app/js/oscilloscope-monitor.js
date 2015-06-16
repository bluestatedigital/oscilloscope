var CircuitBreakerSortOptions = [
    ["errorThenVolume", "Error Then Volume"],
    ["alphabetical", "Alphabetical"],
    ["volume", "Volume"],
    ["error", "Error"],
    ["mean", "Mean"],
    ["median", "Median"],
    ["90th", "90th"],
    ["99th", "99th"],
    ["999th", "99.9th"]
]

var CircuitBreakerLegendOptions = [
    ["success", "Success"],
    ["short_circuited", "Short-Circuited"],
    ["bad_request", "Bad Request"],
    ["timeout", "Timeout"],
    ["rejected", "Rejected"],
    ["failure", "Failure"],
    ["error_percentage", "Error %"]
]

var ThreadPoolSortOptions = [
    ["alphabetical", "Alphabetical"],
    ["volume", "Volume"]
]

var TypeToOptionsMap = {
    "circuitBreakers": {
        displayName: "Circuit Breakers",
        eventType: "HystrixCommand",
        sort: CircuitBreakerSortOptions,
        legend: CircuitBreakerLegendOptions
    },
    "threadPools": {
        displayName: "Thread Pools",
        eventType: "HystrixThreadPool",
        sort: ThreadPoolSortOptions
    }
}

var Container = React.createClass({
    render: function() {
        return (
            <div>
                <div className="row full-width">
                    <Dependency options={TypeToOptionsMap["circuitBreakers"]} />
                </div>

                <div className="row full-width">
                    <Dependency options={TypeToOptionsMap["threadPools"]} />
                </div>
            </div>
        )
    }
})

var Dependency = React.createClass({
    onSortHandler: function(e) {
        console.log(e)
    },
    getLegend: function() {
        if(this.props.options.legend) {
            var options = this.props.options.legend.map(function(o) {
                return <dd className={"legend legend_" + o[0]}>{o[1]}</dd>
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
            return <dd><a data-sort-type={o[0]} onClick={that.onSortHandler}>{o[1]}</a></dd>
        })
        var legend = this.getLegend()

        return (
            <div className="dependency">
                <h3>{this.props.options.displayName}</h3>

                <dl className="sub-nav">
                    <dt>Sort: </dt>
                    {sortOptions}
                </dl>

                {legend}

                <div className="dependency" />
            </div>
        )
    },
    componentDidMount: function() {
        window.addEventListener("hystrix-data", function(e) {
            if(e.type == this.props.options.eventType) {
                this.setState({data: e})
            }
        })
    }
})

React.render(
    <Container />,
    document.getElementById('content')
)