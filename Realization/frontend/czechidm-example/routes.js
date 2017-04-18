module.exports = {
  module: 'example',
  childRoutes: [
    {
      path: '/example/content',
      component: require('./src/content/ExampleContent')
    }
  ]
};
