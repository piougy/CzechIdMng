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
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_ADMIN' ] } ]
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
        },
        {
          path: 'revision',
          component: require('./content/user/Audit'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['AUDIT_READ'] } ]
        }
      ]
    },
    {
      path: 'user/:userID/revision/:revID',
      component: require('./content/user/AuditDetail')
    },
    {
      path: 'users',
      component: require('./content/user/Users'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITY_READ'] } ]
    },
    {
      path: 'users/password/reset',
      component: require('./content/user/PasswordReset')
    },
    {
      path: 'organizations',
      component: require('./content/organization/Organizations'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ORGANIZATION_READ'] } ],
    },
    {
      path: 'organizations/:entityId',
      component: require('./content/organization/OrganizationContent'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ORGANIZATION_READ'] } ]
    },
    {
      path: 'organizations/new',
      component: require('./content/organization/OrganizationContent'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ORGANIZATION_WRITE' ] } ]
    },
    {
      path: 'roles',
      component: require('./content/role/Roles'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ]
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
      path: 'configurations',
      component: require('./content/Configurations'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['CONFIGURATION_WRITE', 'CONFIGURATIONSECURED_READ'] } ]
    },
    {
      path: 'app-modules',
      component: require('./content/AppModules'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_ADMIN'] } ]
    },
    {
      path: 'workflow',
      component: 'div',
      childRoutes: [
        {
          path: 'definitions',
          component: require('./content/workflow/Definitions'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_ADMIN'] } ]
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
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_ADMIN'] } ]
    },
    {
      path: 'workflow/history/processes/:historicProcessInstanceId',
      component: require('./content/workflow/HistoricProcessInstanceDetail')
    },
    {
      path: 'audit/',
      component: 'div',
      childRoutes: [
        {
          path: 'notifications',
          component: require('./content/audit/notification/Notifications'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATION_READ'] } ]
        },
        {
          path: 'notification/:entityId',
          component: require('./content/audit/notification/NotificationContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATION_READ'] } ]
        },
        {
          path: 'emails',
          component: require('./content/audit/email/Emails'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATION_READ'] } ]
        },
        {
          path: 'emails/:entityId',
          component: require('./content/audit/email/EmailContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATION_READ'] } ]
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
