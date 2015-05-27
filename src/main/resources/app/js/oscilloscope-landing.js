$(document).ready(function() {
    // Get the services we know about so we can display a list of them to watch.
    $.ajax("/service/clusters")
        .done(function(result) {
            $.each(result, function(i, e) {
                $("<option></option>")
                    .attr("value", e)
                    .html(e)
                    .appendTo("#cluster")
            })

            $('#cluster').chosen().change(function () {
                targetStateChanged()
            })
        })
        .fail(function () {
            $("<option></option>")
                .html("No clusters available!")
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
    var cluster = $("#cluster").val()
    var stream = $("#stream").val()

    if(stream) {
        redirectToStream(stream)
    } else {
        redirectToCluster(cluster)
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
function redirectToCluster(cluster) {
    location.href="/monitor.html?cluster=" + encodeURIComponent(cluster)
}