module.exports = {
  'id': 'example',
  'npmName': 'czechidm-example',
  'backendId': 'example',
  'name': 'Example module for CzechIdM devstack.',
  'description': 'Example module for CzechIdM devstack. This module can be duplicated and renamed for create new optional CzechIdM module.',
  // 'mainStyleFile': 'src/css/main.less',
  'mainRouteFile': 'routes.js',
  'mainComponentDescriptorFile': 'component-descriptor.js',
  'mainLocalePath': 'src/locales/',
  'navigation': {
    'items': [
      {
        'id': 'example-main-menu',
        'labelKey': 'example:content.examples.label',
        'titleKey': 'example:content.examples.title',
        'icon': 'gift',
        'iconColor': '#FF8A80',
        'order': 9,
        'items': [
          {
            'id': 'example-content',
            'type': 'DYNAMIC',
            'section': 'main',
            'labelKey': 'example:content.example.label',
            'titleKey': 'example:content.example.title',
            'order': 10,
            'path': '/example/content',
            'priority': 0
          },
          {
            'id': 'example-products',
            'type': 'DYNAMIC',
            'section': 'main',
            'icon': 'gift',
            'labelKey': 'example:content.example-products.label',
            'titleKey': 'example:content.example-products.title',
            'order': 20,
            'path': '/example/products',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['EXAMPLEPRODUCT_READ'] } ],
            'items': [
              {
                'id': 'example-product-detail',
                'type': 'TAB',
                'labelKey': 'example:content.example-product.detail.basic',
                'order': 10,
                'path': '/example/product/:entityId/detail',
                'icon': 'gift',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['EXAMPLEPRODUCT_READ'] } ]
              },
            ]
          }
        ]
      },
      {
        'id': 'personal-tab-example',
        'parentId': 'identity-profile',
        'type': 'TAB',
        'labelKey': 'example:content.identity.example',
        'order': 10,
        'path': '/example/content',
        'icon': 'gift'
      }
    ]
  }
};
