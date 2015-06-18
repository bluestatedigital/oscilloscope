/** @jsx React.DOM */
var React = require('react')
window.React = React

// Launch our application.
var Application = require('./Application.jsx')
React.render(<Application />, document.getElementById('content'))