module.exports = {
  'id': 'acc',
  'npmName': 'czechidm-acc',
  'backendId': 'acc',
  'name': 'Account managment',
  'description': 'Module for account managment',
  // 'mainStyleFile': 'src/css/main.less',
  'mainRouteFile': 'routes.js',
  'mainLocalePath': 'src/locales/',
  'navigation': {
    'items': [
      {
        'id': 'sys-systems',
        'type': 'DYNAMIC',
        'labelKey': 'acc:content.systems.title',
        'order': 50,
        'priority': 0,
        'path': '/systems',
        'icon': 'link',
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
      },
      {
        'id': 'profile-accounts',
        'parentId': 'user-profile',
        'type': 'TAB',
        'labelKey': 'acc:content.user.accounts',
        'order': 15,
        'priority': 0,
        'path': '/user/:userID/accounts',
        'icon': 'link'
      },
      {
        'id': 'role-systems',
        'type': 'TAB',
        'parentId': 'role-tabs',
        'labelKey': 'acc:content.role.systems.title',
        'titleKey': 'acc:content.role.systems.title',
        'order': 20,
        'path': '/role/:entityId/systems',
        'icon': 'link',
        'access': [ { 'type': 'HAS_ALL_AUTHORITIES', 'authorities': ['ROLE_READ', 'SYSTEM_READ'] } ]
      }
    ]
  }
};
