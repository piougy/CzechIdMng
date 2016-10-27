module.exports = {
  'id': 'core',
  'npmName': 'czechidm-core',
  'name': 'Core',
  'description': 'Core functionallity. Defines basic navigation structure, routes etc. Has lowest priority, could be overriden.',
  'mainStyleFile': 'src/css/main.less',
  'mainRouteFile': 'routes.js',
  'mainComponentDescriptorFile': 'component-descriptor.js',
  'mainLocalePath': 'src/locales/',
  'navigation': {
    'items': [
      {
        'id': 'identity-profile',
        'type': 'DYNAMIC',
        'section': 'main',
        'label': 'Profil',
        'labelKey': 'navigation.menu.profile.label',
        'title': 'Můj profil',
        'titleKey': 'navigation.menu.profile.title',
        'icon': 'user',
        'iconColor': '#428BCA',
        'order': 10,
        'priority': 0,
        'path': '/identity/:loggedUsername/profile',
        'items': [
          {
            'id': 'profile-personal',
            'type': 'TAB',
            'label': 'Osobní údaje',
            'labelKey': 'content.identity.sidebar.profile',
            'order': 10,
            'priority': 0,
            'path': '/identity/:entityId/profile',
            'icon': 'user'
          },
          {
            'id': 'profile-password',
            'type': 'TAB',
            'label': 'Změna hesla',
            'labelKey': 'content.identity.sidebar.password',
            'order': 20,
            'path': '/identity/:entityId/password',
            'icon': 'lock',
            'conditions': [
              'entityId === userContext.username'
            ]
          },
          {
            'id': 'profile-roles',
            'type': 'TAB',
            'labelKey': 'content.identity.sidebar.roles',
            'order': 30,
            'path': '/identity/:entityId/roles',
            'icon': 'fa:universal-access',
            'access': [ { 'type': 'IS_AUTHENTICATED' } ]
          },
          {
            'id': 'profile-contracts',
            'type': 'TAB',
            'labelKey': 'entity.IdentityContract._type',
            'order': 50,
            'path': '/identity/:entityId/contracts',
            'icon': 'fa:building',
            'access': [ { 'type': 'IS_AUTHENTICATED' } ]
          },
          {
            'id': 'profile-audit',
            'type': 'TAB',
            'labelKey': 'entity.Audit.label',
            'order': 500,
            'path': '/identity/:entityId/revision',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['AUDIT_READ'] } ],
            'icon': 'fa:history',
            'items': [
              {
                'id': 'profile-audit-profile-personal',
                'type': 'TAB',
                'label': 'Osobní údaje',
                'labelKey': 'content.identity.sidebar.profile',
                'order': 10,
                'path': '/identity/:entityId/revision/:revID',
                'icon': 'user'
              }
            ]
          },
          {
            'id': 'profile-subordinates',
            'type': 'TAB',
            'labelKey': 'content.identity.subordinates.title',
            'order': 60,
            'path': '/identity/:entityId/subordinates',
            'icon': 'fa:group',
            'access': [ { 'type': 'IS_AUTHENTICATED' } ]
          }
        ]
      },
      {
        'id': 'tasks',
        'disabled': false,
        'label': 'Úkoly',
        'labelKey': 'navigation.menu.tasks.label',
        'title': 'Moje úkoly',
        'titleKey': 'navigation.menu.tasks.title',
        'icon': 'tasks',
        'path': '/tasks/:entityId',
        'order': 30
      },
      {
        'id': 'identities',
        'labelKey': 'navigation.menu.identities.label',
        'titleKey': 'navigation.menu.identities.title',
        'icon': 'fa:group',
        'order': 1010,
        'path': '/identities',
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITY_READ'] } ]
      },
      {
        'id': 'tree',
        'labelKey': 'content.tree.header',
        'titleKey': 'content.tree.title',
        'icon': 'tree-deciduous',
        'order': 1050,
        'iconColor': '#419641',
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREENODE_WRITE'] } ],
        'items': [
          {
            'id': 'tree-nodes',
            'labelKey': 'content.tree.nodes.title',
            'order': 15,
            'icon': 'apple',
            'path': '/tree/nodes',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREENODE_WRITE'] } ]
          },
          {
            'id': 'tree-types',
            'labelKey': 'content.tree.types.title',
            'order': 10,
            'icon': 'tree-deciduous',
            'path': '/tree/types',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREETYPE_WRITE'] } ]
          }
        ]
      },
      {
        'id': 'profile-system-separator',
        'type': 'SEPARATOR',
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ', 'IDENTITY_READ', 'NOTIFICATION_READ', 'CONFIGURATION_WRITE', 'MODULE_READ'] } ],
        'labelKey': 'navigation.menu.separator.system',
        'order': 999
      },
      {
        'id': 'roles',
        'type': 'DYNAMIC',
        'labelKey': 'content.roles.header',
        'titleKey': 'content.roles.title',
        'icon': 'fa:universal-access',
        'iconColor': '#eb9316',
        'order': 1020,
        'path': '/roles',
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ],
        'items': [
          {
            'id': 'role-detail',
            'type': 'TAB',
            'labelKey': 'content.roles.tabs.basic',
            'order': 10,
            'path': '/role/:entityId/detail',
            'icon': 'fa:newspaper-o'
          },
          {
            'id': 'role-identities',
            'type': 'TAB',
            'labelKey': 'content.role.identities.title',
            'order': 20,
            'path': '/role/:entityId/identities',
            'icon': 'fa:group'
          }
        ]
      },
      {
        'id': 'audit',
        'labelKey': 'content.audit.title',
        'icon': 'stats',
        'order': 1900,
        'items': [
          {
            'id': 'workflow-historic-processes',
            'labelKey': 'navigation.menu.workflow.historicProcess',
            'order': 10,
            'icon': 'fa:sitemap',
            'iconColor': '#428BCA',
            'path': '/workflow/history/processes',
          },
          {
            'id': 'notifications',
            'labelKey': 'content.notifications.title',
            'order': 30,
            'path': '/audit/notifications',
            'icon': 'fa:envelope',
            'access': [
              {
                'type': 'HAS_ANY_AUTHORITY',
                'authorities': ['NOTIFICATION_READ']
              }
            ]
          },
          {
            'id': 'emails',
            'labelKey': 'content.emails.title',
            'order': 35,
            'path': '/audit/emails',
            'icon': 'fa:envelope-o',
            'access': [
              {
                'type': 'HAS_ANY_AUTHORITY',
                'authorities': ['NOTIFICATION_READ']
              }
            ]
          }
        ]
      },
      {
        'id': 'system',
        'labelKey': 'navigation.menu.system',
        'icon': 'cog',
        'order': 2000,
        'path': '/configurations',
        'iconColor': '#c12e2a',
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['CONFIGURATION_WRITE', 'CONFIGURATIONSECURED_READ', 'MODULE_READ'] } ],
        'items': [
          {
            'id': 'system-configuration',
            'labelKey': 'navigation.menu.configuration',
            'icon': 'cog',
            'order': 20,
            'path': '/configurations',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['CONFIGURATION_WRITE', 'CONFIGURATIONSECURED_READ'] } ]
          },
          {
            'id': 'fe-modules',
            'labelKey': 'content.system.fe-modules.title',
            'order': 30,
            'path': '/fe-modules',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['APP_ADMIN'] } ]
          },
          {
            'id': 'be-modules',
            'labelKey': 'content.system.be-modules.title',
            'order': 30,
            'path': '/be-modules',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['MODULE_READ'] } ]
          },
          {
            'id': 'workflow-definitions',
            'labelKey': 'navigation.menu.workflow.definitions',
            'icon': 'fa:sitemap',
            'order': 25,
            'iconColor': '#428BCA',
            'path': '/workflow/definitions',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['APP_ADMIN'] } ]
          },
          {
            'id': 'role-catalogues',
            'labelKey': 'content.roleCatalogues.header',
            'titleKey': 'content.roleCatalogues.title',
            'icon': 'fa:list-alt',
            'iconColor': '#dad727',
            'order': 40,
            'path': '/role-catalogues',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ],
          }
        ]
      },
      {
        'id': 'identities-profile-system',
        'section': 'system',
        'label': 'Můj profil',
        'labelKey': 'navigation.menu.userLabel',
        'icon': 'user',
        'order': 10,
        'path': '/identity/:entityId/profile'
      },
      {
        'id': 'messages',
        'section': 'system',
        'titleKey': 'navigation.menu.messages',
        'icon': 'envelope',
        'order': 20,
        'path': '/messages'
      },
      {
        'id': 'logout',
        'section': 'system',
        'titleKey': 'navigation.menu.logout',
        'icon': 'off',
        'order': 100,
        'path': '/logout'
      },
      {
        'id': 'password-change',
        'section': 'main',
        'labelKey': 'content.password.change.title',
        'order': 10,
        'path': '/password/change',
        'icon': false,
        'access': [ { 'type': 'NOT_AUTHENTICATED' } ]
      },
      {
        'id': 'password-reset',
        'section': 'main',
        'labelKey': 'content.password.reset.title',
        'order': 20,
        'path': '/password/reset',
        'access': [ { 'type': 'DENY_ALL' } ]
      }
    ]
  }
};
