'use strict';

import config from '../../../config.json';

module.exports = {
  'id': 'core',
  'name': 'Core',
  'description': 'Core functionality. Defines basic navigation structure, routes etc.. Has lowest prioryty for overriding.',
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
            'path': '/user/:userID/profile'
          },
          {
            'id': 'profile-password',
            'type': 'TAB',
            'label': 'Změna hesla',
            'labelKey': 'content.user.sidebar.password',
            'order': 20,
            'path': '/user/:userID/password',
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
            'access': [ { 'type': 'IS_AUTHENTICATED' } ]
          },
          {
            'id': 'profile-accounts',
            'type': 'TAB',
            'labelKey': 'content.user.sidebar.accounts',
            'order': 40,
            'path': '/user/:userID/accounts',
            'access': [ { 'type': 'DENY_ALL', 'roles': [config.roles.superAdminRole] } ]
          },
          {
            'id': 'profile-approve',
            'type': 'TAB',
            'labelKey': 'content.user.sidebar.approve',
            'order': 40,
            'path': '/user/:userID/approve',
            'access': [ { 'type': 'DENY_ALL', 'roles': ['superAdminRole2'] } ]
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
        'access': [ { 'type': 'HAS_ANY_ROLE', 'roles': [config.roles.superAdminRole] } ]
      },
      {
        'id': 'organizations',
        'labelKey': 'content.organizations.header',
        'titleKey': 'content.organizations.title',
        'icon': 'fa:group',
        'order': 50,
        'path': '/organizations',
        'access': [ { 'type': 'HAS_ANY_ROLE', 'roles': [config.roles.superAdminRole] } ]
      },
      {
        'id': 'roles',
        'labelKey': 'content.roles.header',
        'titleKey': 'content.roles.title',
        'icon': 'fa:group',
        'order': 35,
        'path': '/roles',
        'access': [ { 'type': 'HAS_ANY_ROLE', 'roles': [config.roles.superAdminRole] } ]
      },
      {
        'id': 'system',
        'labelKey': 'navigation.menu.system',
        'icon': 'cog',
        'order': 1000,
        'path': '/setting',
        'access': [ { 'type': 'HAS_ANY_ROLE', 'roles': [config.roles.superAdminRole] } ],
        'items': [
          {
            'id': 'audit',
            'labelKey': 'content.audit.title',
            'icon': 'stats',
            'order': 10,
            'access': [
              {
                'type': 'DENY_ALL',
                'roles': [ 'auditor', 'superAdminRole2' ]
              }
            ],
            'items': [
              {
                'id': 'audit-log-for-object',
                'labelKey': 'content.audit.object.title',
                'order': 10,
                'path': '/audit/object',
                'access': [
                  {
                    'type': 'HAS_ANY_ROLE',
                    'roles': [ 'auditor', 'superAdminRole2' ]
                  }
                ]
              },
              {
                'id': 'audit-log',
                'labelKey': 'content.audit.log.title',
                'order': 20,
                'path': '/audit/log',
                'access': [
                  {
                    'type': 'HAS_ANY_ROLE',
                    'roles': [ 'auditor', 'superAdminRole2' ]
                  }
                ]
              },
              {
                'id': 'email-log',
                'labelKey': 'content.audit.emailLog.title',
                'order': 30,
                'path': '/audit/email-log',
                'access': [
                  {
                    'type': 'HAS_ANY_ROLE',
                    'roles': [ 'auditor', 'superAdminRole2' ]
                  }
                ]
              }
            ]
          },
          {
            'id': 'system-setting',
            'labelKey': 'navigation.menu.setting',
            'order': 20,
            'path': '/setting',
            'access': [ { 'type': 'DENY_ALL', 'roles': [config.roles.superAdminRole] } ]
          },
          {
            'id': 'system-modules',
            'labelKey': 'content.system.app-modules.title',
            'order': 30,
            'path': '/app-modules',
            'access': [ { 'type': 'HAS_ANY_ROLE', 'roles': [config.roles.superAdminRole] } ]
          },
          {
            'id': 'workflow-definitions',
            'labelKey': 'navigation.menu.workflow.definitions',
            'order': 40,
            'path': '/workflow/definitions',
            'access': [ { 'type': 'HAS_ANY_ROLE', 'roles': [config.roles.superAdminRole] } ]
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
}
