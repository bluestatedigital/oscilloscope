const path = require('path');
const webpack = require('webpack');

module.exports = {
  entry: {
    app: [
      './app/index.js'
    ]
  },
  output: {
    path: './src/main/resources/app/assets',
    filename: 'app.js'
  },
  debug: true,
  devtool: 'inline-source-map',
  module: {
    loaders: [
      {
        test: /\.js$/,
        include: path.join(__dirname, 'app'),
        loader: 'babel',
        query: {
          optional: ['runtime']
        }
      },
      {
        test: /\.jsx$/,
        loaders: ['react-hot', 'babel'],
        include: path.join(__dirname, 'app/components')
      },
      {
        test: /\.css$/,
        loader: "style-loader!css-loader",
        include: [
          path.join(__dirname, 'app/stylesheets'),
          path.join(__dirname, 'node_modules')
        ]
      }
    ]
  },
  plugins: [
    new webpack.NoErrorsPlugin(),
    new webpack.ProvidePlugin({
      $: "jquery",
      jquery: "jquery",
      "window.jQuery": "jquery"
    })
  ]
};
