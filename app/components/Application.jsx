var React = require('react')
var Link = require('react-router').Link
var Configuration = require('../lib/configuration.js')

var Application = React.createClass({
  getInitialState: function() {
    return {menuState: "closed"}
  },
  handleMenuClick: function(e) {
    e.preventDefault()

    if(this.state.menuState == "expanded") {
      this.setState({menuState: "closed"})
    } else {
      this.setState({menuState: "expanded"})
    }
  },
  render: function() {
    var monitorMode = ''
    if(this.props.params && this.props.params.mode) {
      var modeDisplayString = Configuration.getModeDisplayString(this.props.params.data)
      monitorMode = (
          <section className="top-bar-section">
            <ul className="left">
              <li><span className="status">{modeDisplayString}</span></li>
            </ul>
            <ul className="right">
              <li className="divider"></li>
              <li><Link to={`/`}>Reset</Link></li>
            </ul>
          </section>
      )
    }

    var topbarState = "top-bar " + this.state.menuState

    return (
      <div>
        <nav className={topbarState}>
          <ul className="title-area">
            <li className="name">
              <h3><span>Oscilloscope</span></h3>
            </li>
            <li className="toggle-topbar menu-icon"><a href="#" onClick={this.handleMenuClick}><span>Menu</span></a></li>
          </ul>
          {monitorMode}
        </nav>
        {this.props.children}
      </div>
    )
  }
})

module.exports = Application
