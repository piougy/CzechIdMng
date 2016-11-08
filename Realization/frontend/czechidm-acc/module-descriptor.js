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
        'order': 1100,
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
            'order': 30,
            'path': '/system/:entityId/entities',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
          },
          {
            'id': 'system-object-classes',
            'type': 'TAB',
            'labelKey': 'acc:content.system.system-object-classes.title',
            'order': 40,
            'path': '/system/:entityId/object-classes',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
          },
          {
            'id': 'system-entities-handling',
            'type': 'TAB',
            'icon': 'list-alt',
            'labelKey': 'acc:content.system.system-entities-handling.title',
            'order': 50,
            'path': '/system/:entityId/entities-handling',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
          }
        ]
      },
      {
        'id': 'identity-accounts',
        'parentId': 'identity-profile',
        'type': 'TAB',
        'labelKey': 'acc:content.identity.accounts.title',
        'order': 100,
        'priority': 0,
        'path': '/identity/:entityId/accounts',
        'icon': 'fa:external-link'
      },
      {
        'id': 'role-systems',
        'type': 'TAB',
        'parentId': 'roles',
        'labelKey': 'acc:content.role.systems.title',
        'titleKey': 'acc:content.role.systems.title',
        'order': 50,
        'path': '/role/:entityId/systems',
        'icon': 'link',
        'access': [ { 'type': 'HAS_ALL_AUTHORITIES', 'authorities': ['ROLE_READ', 'SYSTEM_READ'] } ]
      }
    ]
  }
};
