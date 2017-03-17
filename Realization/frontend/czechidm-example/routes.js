module.exports = {
  module: 'example',
  component: 'div',
  childRoutes: [
    {
      path: '/example/content',
      component: require('./src/content/ExampleContent')
    }
  ]
};
