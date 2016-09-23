module.exports = {
  component: 'div',
  childRoutes: [
    {
      path: 'login',
      component: require('./src/content/Login'),
      access: [ { type: 'PERMIT_ALL' } ]
    },
    {
      path: 'logout',
      component: require('./src/content/Logout'),
      access: [ { type: 'PERMIT_ALL' } ]
    },
    {
      path: 'password/reset',
      component: require('./src/content/PasswordReset'),
      access: [ { type: 'PERMIT_ALL' } ]
    },
    {
      path: 'password/change',
      component: require('./src/content/PasswordChange'),
      access: [ { type: 'PERMIT_ALL' } ]
    },
    {
      path: 'user/new',
      component: require('./src/content/user/Create'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_ADMIN' ] } ]
    },
    {
      path: 'user/:userID/',
      component: require('./src/content/user/User'),
      childRoutes: [
        {
          path: 'profile',
          component: require('./src/content/user/Profile')
        },
        {
          path: 'password',
          component: require('./src/content/user/PasswordChange')
        },
        {
          path: 'roles',
          component: require('./src/content/user/Roles')
        },
        {
          path: 'workingPositions',
          component: require('./src/content/user/WorkingPositions')
        },
        {
          path: 'revision',
          component: require('./src/content/user/Audit'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['AUDIT_READ'] } ]
        }
      ]
    },
    {
      path: 'user/:userID/revision/:revID',
      component: require('./src/content/user/AuditDetail')
    },
    {
      path: 'users',
      component: require('./src/content/user/Users'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITY_READ'] } ]
    },
    {
      path: 'users/password/reset',
      component: require('./src/content/user/PasswordReset')
    },
    {
      path: 'organizations',
      component: require('./src/content/organization/Organizations'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ORGANIZATION_READ'] } ],
    },
    {
      path: 'organizations/:entityId',
      component: require('./src/content/organization/OrganizationContent'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ORGANIZATION_READ'] } ]
    },
    {
      path: 'organizations/new',
      component: require('./src/content/organization/OrganizationContent'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ORGANIZATION_WRITE' ] } ]
    },
    {
      path: 'roles',
      component: require('./src/content/role/Roles'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ],
    },
    {
      path: 'role/:entityId/',
      component: require('./src/content/role/Role'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_WRITE'] } ],
      childRoutes: [
        {
          path: 'detail',
          component: require('./src/content/role/Content')
        },
      ]
    },
    {
      path: 'role/:entityId/new',
      component: require('./src/content/role/Content'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_WRITE'] } ],
    },
    {
      path: 'tasks/:userID',
      component: require('./src/content/task/TaskInstances'),
      access: [ { 'type': 'IS_AUTHENTICATED'}]
    },
    {
      path: 'task/:taskID',
      component: require('./src/content/task/Task')
    },
    {
      path: 'messages',
      component: require('./src/content/Messages')
    },
    {
      path: 'configurations',
      component: require('./src/content/Configurations'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['CONFIGURATION_WRITE', 'CONFIGURATIONSECURED_READ'] } ]
    },
    {
      path: 'app-modules',
      component: require('./src/content/AppModules'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_ADMIN'] } ]
    },
    {
      path: 'be-modules',
      component: require('./src/content/module/BackendModules'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['MODULE_READ'] } ]
    },
    {
      path: 'workflow',
      component: 'div',
      childRoutes: [
        {
          path: 'definitions',
          component: require('./src/content/workflow/Definitions'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_ADMIN'] } ]
        },
        {
          path: 'history/processes',
          component: require('./src/content/workflow/HistoricProcessInstances')
        }
      ]
    },
    {
      path: 'workflow/definitions/:definitionId',
      component: require('./src/content/workflow/Definition'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_ADMIN'] } ]
    },
    {
      path: 'workflow/history/processes/:historicProcessInstanceId',
      component: require('./src/content/workflow/HistoricProcessInstanceDetail')
    },
    {
      path: 'audit/',
      component: 'div',
      childRoutes: [
        {
          path: 'notifications',
          component: require('./src/content/audit/notification/Notifications'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATION_READ'] } ]
        },
        {
          path: 'notification/:entityId',
          component: require('./src/content/audit/notification/NotificationContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATION_READ'] } ]
        },
        {
          path: 'emails',
          component: require('./src/content/audit/email/Emails'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATION_READ'] } ]
        },
        {
          path: 'emails/:entityId',
          component: require('./src/content/audit/email/EmailContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATION_READ'] } ]
        },
      ]
    },
    // About site
    {
      path: 'about',
      component: require('./src/content/About')
    },
    // error pages
    {
      path: 'unavailable',
      component: require('./src/content/error/503'),
      access: [ { type: 'PERMIT_ALL' } ]
    },
    {
      path: 'error/403',
      component: require('./src/content/error/403'),
      access: [ { type: 'PERMIT_ALL' } ]
    },
    {
      path: '*',
      component: require('./src/content/error/404'),
      access: [ { type: 'PERMIT_ALL' } ]
    }
  ]
};
