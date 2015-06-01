//Read a page's GET URL variables and return them as an associative array.
// from: http://jquery-howto.blogspot.com/2009/09/get-url-parameters-values-with-jquery.html
function getUrlVars()
{
    var vars = []
    var hash = []

    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&')
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=')
        vars.push(hash[0])
        vars[hash[0]] = hash[1]
    }

    return vars
}

// Create our monitors.  We leave them out here because we need to access them from the monitor
// page to sort differently, etc.
var hystrixMonitor = new HystrixCommandMonitor('dependencies', {includeDetailIcon: false})
var dependencyThreadPoolMonitor = new HystrixThreadPoolMonitor('dependencyThreadPools')

$(document).ready(function() {
    // Get our stream/cluster values, and build our command/pool stream sources based on which one we have.
    var stream = getUrlVars()["stream"]
    var cluster = getUrlVars()["cluster"]

    var eventStream = ""

    if (stream != undefined) {
        eventStream = "/service/stream/host?target=" + stream

        $("#monitor_type").html("Stream: <small>" + decodeURIComponent(stream) + "</small>")
    } else if (cluster != undefined) {
        eventStream = "/service/stream/cluster?cluster=" + cluster

        $("#monitor_type").html("Cluster: <small>" + cluster + "</small>")
    }

    // Now load both our command and pool monitors asynchronously to avoid infinite spinner situations.
    $(window).load(function () {
        setTimeout(function () {
            if (eventStream == "") {
                $("#dependencies .loading").html("The 'stream' or 'cluster' argument was not provided.")
                $("#dependencies .loading").addClass("failed")
                $("#dependencyThreadPools .loading").html("The 'stream' or 'cluster' argument was not provided.")
                $("#dependencyThreadPools .loading").addClass("failed")
            } else {
                // Sort by error+volume by default.
                hystrixMonitor.sortByErrorThenVolume()

                // Sort by volume by default.
                dependencyThreadPoolMonitor.sortByVolume()

                // Create our EventSource object which will immediately start streaming data from the server.
                var source = new EventSource(eventStream)

                // Now add our event listeners to actually process events and handle any errors.
                source.addEventListener('message', hystrixMonitor.eventSourceMessageListener, false)
                source.addEventListener('message', dependencyThreadPoolMonitor.eventSourceMessageListener, false)
                source.addEventListener('error', function (e) {
                    $("#dependencies .loading").html("Failed to connect to the command stream.")
                    $("#dependencies .loading").addClass("failed");
                    $("#dependencyThreadPools .loading").html("Failed to connect to the pool stream.")
                    $("#dependencyThreadPools .loading").addClass("failed")

                    if (e.eventPhase == EventSource.CLOSED) {
                        // Connection was closed.
                        console.log("Connection was closed on error: " + e)
                    } else {
                        console.log("Error occurred while streaming: " + e)
                    }
                }, false)
            }
        }, 0)
    })
})

$(document).ready(function() {
    $(".sub-nav dd").on("click", function() {
        if(!$(this).hasClass("active")) {
            $(this).parent().children("dd.active").removeClass("active")
            $(this).addClass("active")
        }
    })
})