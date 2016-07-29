

import config from '../../../config.json';

module.exports = {
  component: 'div',
  childRoutes: [
    {
      path: 'login',
      component: require('./content/Login'),
      access: [ { type: 'PERMIT_ALL' } ]
    },
    {
      path: 'logout',
      component: require('./content/Logout'),
      access: [ { type: 'PERMIT_ALL' } ]
    },
    {
      path: 'password/reset',
      component: require('./content/PasswordReset'),
      access: [ { type: 'PERMIT_ALL' } ]
    },
    {
      path: 'password/change',
      component: require('./content/PasswordChange'),
      access: [ { type: 'PERMIT_ALL' } ]
    },
    {
      path: 'user/new',
      component: require('./content/user/Create'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority ] } ]
    },
    {
      path: 'user/:userID/',
      component: require('./content/user/User'),
      childRoutes: [
        {
          path: 'profile',
          component: require('./content/user/Profile')
        },
        {
          path: 'password',
          component: require('./content/user/PasswordChange')
        },
        {
          path: 'roles',
          component: require('./content/user/Roles')
        },
        {
          path: 'accounts',
          component: require('./content/user/Accounts')
        },
        {
          path: 'subordinates',
          component: require('./content/user/Subordinates')
        },
        {
          path: 'workingPositions',
          component: require('./content/user/WorkingPositions')
        },
        {
          path: 'approve',
          component: require('./content/user/Approve')
        },
        {
          path: 'delegates',
          component: require('./content/user/Delegates')
        }
      ]
    },
    {
      path: 'users',
      component: require('./content/user/Users'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ]
    },
    {
      path: 'users/password/reset',
      component: require('./content/user/PasswordReset')
    },
    {
      path: 'organizations',
      component: require('./content/Organizations'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ]
    },
    {
      path: 'roles',
      component: require('./content/role/Roles'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ]
    },
    {
      path: 'tasks/:userID',
      component: require('./content/task/TaskInstances'),
      access: [ { 'type': 'IS_AUTHENTICATED'}]
    },
    {
      path: 'task/:taskID',
      component: require('./content/task/Task')
    },
    {
      path: 'messages',
      component: require('./content/Messages')
    },
    {
      path: 'setting',
      component: require('./content/Setting'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ]
    },
    {
      path: 'app-modules',
      component: require('./content/AppModules'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ]
    },
    {
      path: 'workflow',
      component: 'div',
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ],
      childRoutes: [
        {
          path: 'definitions',
          component: require('./content/workflow/Definitions')
        },
        {
          path: 'history/processes',
          component: require('./content/workflow/HistoricProcessInstances')
        }
      ]
    },
    {
      path: 'workflow/definitions/:definitionId',
      component: require('./content/workflow/Definition'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ]
    },
    {
      path: 'workflow/history/processes/:historicProcessInstanceId',
      component: require('./content/workflow/HistoricProcessInstanceDetail'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ]
    },
    {
      path: 'audit/',
      component: 'div',
      childRoutes: [
        {
          path: 'notifications',
          component: require('./content/audit/notification/Notifications'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ]
        },
        {
          path: 'notification/:entityId',
          component: require('./content/audit/notification/NotificationContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority] } ]
        },
      ]
    },
    // error pages
    {
      path: 'unavailable',
      component: require('./content/error/503'),
      access: [ { type: 'PERMIT_ALL' } ]
    },
    {
      path: 'error/403',
      component: require('./content/error/403'),
      access: [ { type: 'PERMIT_ALL' } ]
    },
    {
      path: '*',
      component: require('./content/error/404'),
      access: [ { type: 'PERMIT_ALL' } ]
    }
  ]
};
