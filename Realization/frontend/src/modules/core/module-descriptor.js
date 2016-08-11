

import config from '../../../config.json';

module.exports = {
  'id': 'core',
  'name': 'Core',
  'description': 'Core functionallity. Defines basic navigation structure, routes etc. Has lowest priority, could be overriden.',
  'navigation': {
    'items': [
      {
        'id': 'user-profile',
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
        'path': '/user/:loggedUsername/profile',
        'items': [
          {
            'id': 'profile-personal',
            'type': 'TAB',
            'label': 'Osobní údaje',
            'labelKey': 'content.user.sidebar.profile',
            'order': 10,
            'priority': 0,
            'path': '/user/:userID/profile',
            'icon': 'user'
          },
          {
            'id': 'profile-password',
            'type': 'TAB',
            'label': 'Změna hesla',
            'labelKey': 'content.user.sidebar.password',
            'order': 20,
            'path': '/user/:userID/password',
            'icon': 'lock',
            'conditions': [
              'userID === userContext.username'
            ]
          },
          {
            'id': 'profile-roles',
            'type': 'TAB',
            'labelKey': 'content.user.sidebar.roles',
            'order': 30,
            'path': '/user/:userID/roles',
            'icon': 'fa:group',
            'access': [ { 'type': 'IS_AUTHENTICATED' } ]
          },
          {
            'id': 'profile-accounts',
            'type': 'TAB',
            'labelKey': 'content.user.sidebar.accounts',
            'order': 40,
            'path': '/user/:userID/accounts',
            'access': [ { 'type': 'DENY_ALL' } ]
          },
          {
            'id': 'profile-approve',
            'type': 'TAB',
            'labelKey': 'content.user.sidebar.approve',
            'order': 40,
            'path': '/user/:userID/approve',
            'access': [ { 'type': 'DENY_ALL' } ]
          },
          {
            'id': 'profile-subordinates',
            'type': 'TAB',
            'labelKey': 'content.user.sidebar.subordinates',
            'order': 60,
            'path': '/user/:userID/subordinates',
            'access': [ { 'type': 'DENY_ALL' } ]
          },
          {
            'id': 'profile-working-positions',
            'type': 'TAB',
            'labelKey': 'entity.IdentityWorkingPosition._type',
            'order': 50,
            'path': '/user/:userID/workingPositions',
            'icon': 'fa:building',
            'access': [ { 'type': 'IS_AUTHENTICATED' } ]
          },
          {
            'id': 'profile-delegates',
            'type': 'TAB',
            'labelKey': 'content.user.delegates.title',
            'order': 70,
            'path': '/user/:userID/delegates',
            'access': [ { 'type': 'DENY_ALL' } ]
          }
        ]
      },
      {
        'id': 'user-subordinates',
        'label': 'Podřízení',
        'labelKey': 'navigation.menu.subordinates.label',
        'title': 'Můj tým',
        'titleKey': 'navigation.menu.subordinates.title',
        'icon': 'briefcase',
        'order': 20,
        'path': '/user/:loggedUsername/subordinates',
        'includeItemsId': 'user-profile',
        'access': [ { 'type': 'DENY_ALL' } ]
      },
      {
        'id': 'tasks',
        'disabled': false,
        'label': 'Úkoly',
        'labelKey': 'navigation.menu.tasks.label',
        'title': 'Moje úkoly',
        'titleKey': 'navigation.menu.tasks.title',
        'icon': 'tasks',
        'path': '/tasks/:userID',
        'order': 30
      },
      {
        'id': 'users',
        'labelKey': 'navigation.menu.users.label',
        'titleKey': 'navigation.menu.users.title',
        'icon': 'user',
        'order': 40,
        'path': '/users',
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ]
      },
      {
        'id': 'organizations',
        'labelKey': 'content.organizations.header',
        'titleKey': 'content.organizations.title',
        'icon': 'fa:building',
        'order': 50,
        'iconColor': '#eb9316',
        'path': '/organizations',
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ]
      },
      {
        'id': 'roles',
        'labelKey': 'content.roles.header',
        'titleKey': 'content.roles.title',
        'icon': 'fa:group',
        'iconColor': '#419641',
        'order': 35,
        'path': '/roles',
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ]
      },
      {
        'id': 'workflow',
        'labelKey': 'navigation.menu.workflow.title',
        'icon': 'fa:sitemap',
        'order': 40,
        'iconColor': '#428BCA',
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ],
        'items': [
          {
            'id': 'workflow-definitions',
            'labelKey': 'navigation.menu.workflow.definitions',
            'order': 40,
            'path': '/workflow/definitions',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ]
          },
          {
            'id': 'workflow-historic-processes',
            'labelKey': 'navigation.menu.workflow.historicProcess',
            'order': 35,
            'path': '/workflow/history/processes',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ]
          }
        ]
      },
      {
        'id': 'audit',
        'labelKey': 'content.audit.title',
        'icon': 'stats',
        'order': 1010,
        'access': [
          {
            'type': 'HAS_ANY_AUTHORITY',
            'authorities': [ 'SYSTEM_ADMIN' ]
          }
        ],
        'items': [
          {
            'id': 'notifications',
            'labelKey': 'content.notifications.title',
            'order': 30,
            'path': '/audit/notifications',
            'icon': 'fa:envelope',
            'access': [
              {
                'type': 'HAS_ANY_AUTHORITY',
                'authorities': [config.authorities.superAdminAuthority]
              }
            ]
          },
          {
            'id': 'emails',
            'labelKey': 'content.emails.title',
            'order': 30,
            'path': '/audit/emails',
            'icon': 'fa:envelope-o',
            'access': [
              {
                'type': 'HAS_ANY_AUTHORITY',
                'authorities': [config.authorities.superAdminAuthority]
              }
            ]
          }
        ]
      },
      {
        'id': 'system',
        'labelKey': 'navigation.menu.system',
        'icon': 'cog',
        'order': 1000,
        'path': '/configuration',
        'iconColor': '#c12e2a',
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ],
        'items': [
          {
            'id': 'system-configuration',
            'labelKey': 'navigation.menu.configuration',
            'icon': 'cog',
            'order': 20,
            'path': '/configuration',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ]
          },
          {
            'id': 'system-modules',
            'labelKey': 'content.system.app-modules.title',
            'order': 30,
            'path': '/app-modules',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ]
          }
        ]
      },
      {
        'id': 'user-profile-system',
        'section': 'system',
        'label': 'Můj profil',
        'labelKey': 'navigation.menu.userLabel',
        'icon': 'user',
        'order': 10,
        'path': '/user/:userID/profile'
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
        'access': [ { 'type': 'DENY_ALL' } ]
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
