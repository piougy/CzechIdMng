module.exports = {
  module: 'example',
  childRoutes: [
    {
      path: '/example/content',
      component: require('./src/content/ExampleContent')
    },
    {
      path: '/example/products',
      component: require('./src/content/example-product/ExampleProducts')
    },
    {
      path: 'example/product/:entityId/',
      component: require('./src/content/example-product/ExampleProductRoute'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['EXAMPLEPRODUCT_READ'] } ],
      childRoutes: [
        {
          path: 'detail',
          component: require('./src/content/example-product/ExampleProductContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['EXAMPLEPRODUCT_READ'] } ]
        }
      ]
    },
    {
      path: 'example/product/:entityId/new',
      component: require('./src/content/example-product/ExampleProductContent'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['EXAMPLEPRODUCT_CREATE'] } ]
    }
  ]
};
