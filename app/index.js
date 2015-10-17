import domready from 'domready'
import React from 'react'
import ReactDOM from 'react-dom'
import {Router, Route, IndexRoute} from 'react-router'
import createHistory from 'history/lib/createHashHistory'
import Application from './components/Application.jsx'
import Selection from './components/Selection.jsx'
import Monitoring from './components/Monitoring.jsx'

// Pull in all of our CSS because we're going to need it.
require('./stylesheets/normalize.css')
require('./stylesheets/foundation.css')
require('./stylesheets/oscilloscope.css')

// Override the history used by react-router so we don't have that ugly URL parameter.
var history = createHistory({
    queryKey: false
})

domready(() => {
  ReactDOM.render((
    <Router history={history}>
      <Route path="/" component={Application}>
        <IndexRoute component={Selection} />
        <Route path="/monitor/:mode/:data" component={Monitoring} />
      </Route>
    </Router>
  ), document.getElementById('application'))
})
