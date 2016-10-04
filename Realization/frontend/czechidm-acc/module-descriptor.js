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
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ],
        'items': [
          {
            'id': 'system-detail',
            'type': 'TAB',
            'labelKey': 'acc:content.system.detail.basic',
            'order': 1,
            'path': '/system/:entityId/detail',
            'icon': 'fa:newspaper-o',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
          },
          {
            'id': 'system-accounts',
            'type': 'TAB',
            'labelKey': 'acc:content.system.accounts.title',
            'order': 20,
            'path': '/system/:entityId/accounts',
            'icon': 'fa:external-link',
            'access': [ { 'type': 'HAS_ALL_AUTHORITIES', 'authorities': ['SYSTEM_READ', 'ACCOUNT_READ'] } ]
          },
          {
            'id': 'system-entities',
            'type': 'TAB',
            'labelKey': 'acc:content.system.entities.title',
            'order': 20,
            'path': '/system/:entityId/entities',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
          }
        ]
      },
      {
        'id': 'profile-accounts',
        'parentId': 'user-profile',
        'type': 'TAB',
        'labelKey': 'acc:content.user.accounts',
        'order': 15,
        'priority': 0,
        'path': '/user/:userID/accounts',
        'icon': 'fa:external-link',
        'access': [ { 'type': 'DENY_ALL' } ]
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
