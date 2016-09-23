module.exports = {
  'id': 'example',
  'npmName': 'czechidm-example',
  'name': 'Example module for CzechIdM 2.0 devstack.',
  'description': 'Example module for CzechIdM 2.0 devstack. This module can be duplicated and renamed for create new optional czechidm module.',
  // 'mainStyleFile': 'src/css/main.less',
  // 'mainRouteFile': 'routes.js',
  'mainLocalePath': 'src/locales/',
  'navigation': {
    'items': [
      {
        'id': 'example-main-menu',
        'type': 'DYNAMIC',
        'section': 'main',
        'labelKey': 'example:content.example.menu',
        'titleKey': 'example:content.example.title',
        'icon': 'gift',
        'iconColor': '#FF8A80',
        'order': 9,
        'path': '/user/:userID/roles',
        'priority': 0
      },
      {
        'id': 'personal-tab-example',
        'parentId': 'user-profile',
        'type': 'TAB',
        'labelKey': 'example:content.user.example',
        'order': 1,
        'priority': 0,
        'path': '/user/:userID/examples',
        'icon': 'gift'
      }
    ]
  }
};
