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
            var alert = $("<div></div>")
                .addClass("alert-box")
                .addClass("warning")
                .html("Failed to get list of available clusters!")

            alert.hide().insertAfter("#cluster").fadeIn(1000)
        })

    $("#stream").on("input propertychange paste", function() {
        targetStateChanged()
    })

    $("#monitor").on("click", function(e) {
        e.stopPropagation()
        redirectToMonitor()
    })
})

function targetStateChanged() {
    var cluster = $("#cluster").val()
    var stream = $("#stream").val()

    if(!stream && !cluster) {
        $("#monitor").attr("disabled", "disabled")
    } else {
        $("#monitor").attr("disabled", false)
    }
}

function redirectToMonitor() {
    var cluster = $("#cluster").val()
    var stream = $("#stream").val()

    if(stream) {
        redirectToStream(stream)
    } else {
        redirectToCluster(cluster)
    }
}

function redirectToStream(stream) {
    console.log("redirect to stream: " + stream)
}

function redirectToCluster(cluster) {
    console.log("redirect to cluster: " + cluster)
}