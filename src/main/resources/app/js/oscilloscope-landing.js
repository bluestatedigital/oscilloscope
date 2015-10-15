$(document).ready(function() {
    // Get the services we know about so we can display a list of them to watch.
    $.ajax("/service/clusters")
        .done(function(result) {
            if(result.length == 0) {
                $("<option></option>")
                    .html("No clusters found.")
                    .attr("selected", "selected")
                    .appendTo("#cluster")

                $("#cluster").attr("disabled", "disabled")
            }

            $.each(result, function(i, e) {
                $("<option></option>")
                    .attr("id", "cluster-entry-" + i)
                    .attr("value", i)
                    .attr("data-cluster-name", e["ClusterName"])
                    .attr("data-cluster-provider", e["ClusterProvider"])
                    .html(e["ClusterName"])
                    .appendTo("#cluster")
            })

            $('#cluster').chosen().change(function () {
                targetStateChanged()
            })
        })
        .fail(function () {
            $("<option></option>")
                .html("Failed to query available clusters!")
                .attr("selected", "selected")
                .appendTo("#cluster")

            $("#cluster").attr("disabled", "disabled")
        })

    // Track changes to our custom target textbox.
    $("#stream").on("input propertychange paste", function() {
        targetStateChanged()
    })

    // Allow users to hit Enter when typing or pasting in a custom stream, rather than make
    // them have to click the 'Monitor Cluster / Target' button.
    $("#stream").on("keypress", function(e) {
        var stream = $("#stream").val()
        if(stream) {
            if(e.which == 13) {
                redirectToStream(stream)
            }
        }
    })

    // Set up our button to take us to the appropriate spot.
    $("#monitor").on("click", function(e) {
        e.stopPropagation()
        redirectToMonitor()
    })
})

/*
    Tracks state changes to the target selectors.

    Since we want to make sure you can't click on the 'Monitor Cluster / Target' button
    if you have no option selected or specified, this keeps track of whether or not we
    have a valid option to use, and disables/enables the button appropriately.
 */
function targetStateChanged() {
    var cluster = $("#cluster").val()
    var stream = $("#stream").val()

    if(!stream && !cluster) {
        $("#monitor").attr("disabled", "disabled")
    } else {
        $("#monitor").attr("disabled", false)
    }
}

/*
    Redirects the user to the monitor page.

    Custom stream targets take precedence over a cluster selection here, since you may have
    chosen something unwittingly, then just wanted to go to a custom location, and now you'd
    otherwise be locked in to your cluster choice, since you can't deselect.
 */
function redirectToMonitor() {
    var clusterId = $("#cluster").val()
    var stream = $("#stream").val()

    if(stream) {
        redirectToStream(stream)
    } else {
        var clusterEntry = $("#cluster-entry-" + clusterId)
        var clusterName = clusterEntry.attr("data-cluster-name")
        var clusterProvider = clusterEntry.attr("data-cluster-provider")

        redirectToCluster(clusterName, clusterProvider)
    }
}

/*
    Redirects the user to the monitor page in custom stream target mode.
 */
function redirectToStream(stream) {
    location.href="/monitor.html?stream=" + encodeURIComponent(stream)
}

/*
    Redirects the user to the monitor page in cluster target mode.
 */
function redirectToCluster(clusterName, clusterProvider) {
    location.href="/monitor.html?cluster=" + clusterName + "&provider=" + clusterProvider
}
