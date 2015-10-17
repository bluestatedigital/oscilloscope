import domready from 'domready'
import React from 'react'
import ReactDOM from 'react-dom'
import Application from './components/Application.jsx'

// Pull in all of our CSS because we're going to need it.
require('./stylesheets/normalize.css')
require('./stylesheets/foundation.css')
require('./stylesheets/oscilloscope.css')

domready(() => {
  ReactDOM.render(<Application />, document.getElementById('application'))
})
