var Utility = {
    getRoundedNumber: function(num) {
        var dec = 1
        var result = Math.round(num*Math.pow(10,dec))/Math.pow(10,dec)
        var resultAsString = result.toString()
        if(resultAsString.indexOf('.') == -1) {
            resultAsString = resultAsString + '.0'
        }

        return resultAsString
    }
}

module.exports = Utility