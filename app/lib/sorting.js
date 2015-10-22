var Sorting = function(anchorKey, sortKey, sortIntervalMs) {
  var state = {
    anchorKey: anchorKey || "name",
    sortKey: sortKey || "name",
    sortIntervalMs: sortIntervalMs || 10000,
    lastSort: 0,
    sortMapping: null
  }

  return {
    // Sets the sort key to use on objects.
    setSortKey: function(key) {
      state.sortKey = key
    },

    // Invalidates the current sort mapping by forcing a resort when the
    // next sort is requested.
    //
    // This is required to support forced resorts if the size of the input array
    // changes or if items are swapped and the *identity* of the items to be
    // sorted is no longer the same.  (We care about identity, but we're sorting
    // on individual values to belong to objects, not the identity itself.)
    invalidate: function() {
      state.lastSort = 0
    },

    // Primary sort method for outside users.
    sort: function(xs) {
      if(state.sortKey == null || state.anchorKey == null) {
        return xs
      }

      return this.sortByMapping(xs)
    },

    // Sorts the input array according to the current sort mapping.
    sortByMapping: function(xs) {
      // Regenerate our sort mapping if need be.
      this.regenerateMapping(xs)

      // Go through each sort mapping and find the corresponding entry in
      // the entry array and spit it to an output array, in sorted order.
      var sorted = []
      for(var i = 0; i < state.sortMapping.length; i++) {
        var matchingItem = this.findByAnchorKey(xs, state.sortMapping[i])
        sorted.push(matchingItem);
      }

      return sorted
    },

    // Checks to see if our current sort mapping needs to be regenerated.
    regenerateMapping: function(xs) {
      // See if we've crossed the threshold where we need to resort again.
      var mappingIsStale = Date.now() > state.lastSort + state.sortIntervalMs
      var mappingDoesntExist = state.sortMapping == null
      if(mappingIsStale || mappingDoesntExist) {
        state.lastSort = Date.now()

        // Sort the array we were given first, then extract the anchor key
        // to generate our sort mapping.
        var sorted = this.sortByKey(xs, state.sortKey)
        state.sortMapping = sorted.map(function(x) { return x[state.anchorKey] })
      }
    },

    findByAnchorKey: function(xs, value) {
      for(var i = 0; i < xs.length; i++) {
        if(xs[i][state.anchorKey] == value) {
          return xs[i]
        }
      }

      return null
    },

    sortByKey: function(xs, key) {
      // Operate on a copy of this.  We don't want to sort in place.
      var xsCopy = xs.slice(0, xs.length-1)
      xsCopy.sort(function(a, b) {
        var valueA = a[key]
        var valueB = b[key]

        if(valueA < valueB) {
          return -1
        }

        if(valueA > valueB) {
          return 1
        }

        return 0
      })

      return xsCopy
    }
  }
}

module.exports = Sorting
