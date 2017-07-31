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
        'type': 'DYNAMIC',
        'section': 'main',
        'labelKey': 'example:content.example.label',
        'titleKey': 'example:content.example.title',
        'icon': 'gift',
        'iconColor': '#FF8A80',
        'order': 9,
        'path': '/example/content',
        'priority': 0
      },
      {
        'id': 'personal-tab-example',
        'parentId': 'identity-profile',
        'type': 'TAB',
        'labelKey': 'example:content.identity.example',
        'order': 1,
        'priority': 0,
        'path': '/identity/:entityId/examples',
        'icon': 'gift'
      }
    ]
  }
};
