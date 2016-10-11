module.exports = {
  module: 'core',
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
      path: 'identity/new',
      component: require('./src/content/identity/Create'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['APP_ADMIN' ] } ]
    },
    {
      path: 'identity/:entityId/',
      component: require('./src/content/identity/Identity'),
      childRoutes: [
        {
          path: 'profile',
          component: require('./src/content/identity/IdentityProfile')
        },
        {
          path: 'password',
          component: require('./src/content/identity/PasswordChange')
        },
        {
          path: 'roles',
          component: require('./src/content/identity/IdentityRoles')
        },
        {
          path: 'contracts',
          component: require('./src/content/identity/IdentityContracts')
        },
        {
          path: 'revision',
          component: require('./src/content/identity/Audit'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['AUDIT_READ'] } ]
        }
      ]
    },
    {
      path: 'identity/:entityId/revision/:revID',
      component: require('./src/content/identity/AuditDetail')
    },
    {
      path: 'identities',
      component: require('./src/content/identity/Identities'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITY_READ'] } ]
    },
    {
      path: 'identities/password/reset',
      component: require('./src/content/identity/PasswordReset')
    },
    {
      path: 'tree',
      childRoutes: [
        {
          path: 'nodes',
          component: require('./src/content/tree/node/Nodes'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREENODE_WRITE'] } ],
        },
        {
          path: 'nodes/:entityId',
          component: require('./src/content/tree/node/NodeContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREENODE_WRITE'] } ]
        },
        {
          path: 'nodes/new',
          component: require('./src/content/tree/node/NodeContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREENODE_WRITE' ] } ]
        },
        {
          path: 'types',
          component: require('./src/content/tree/type/Types'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREETYPE_WRITE'] } ],
        },
        {
          path: 'types/new',
          component: require('./src/content/tree/type/TypeContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREETYPE_WRITE' ] } ]
        },
        {
          path: 'types/:entityId',
          component: require('./src/content/tree/type/TypeContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREETYPE_WRITE'] } ]
        },
      ]
    },
    {
      path: 'roles',
      component: require('./src/content/role/Roles'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ]
    },
    {
      path: 'role/:entityId/',
      component: require('./src/content/role/Role'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_WRITE'] } ],
      childRoutes: [
        {
          path: 'detail',
          component: require('./src/content/role/RoleContent')
        },
      ]
    },
    {
      path: 'role/:entityId/new',
      component: require('./src/content/role/RoleContent'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_WRITE'] } ],
    },
    {
      path: 'tasks/:entityId',
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
      path: 'fe-modules',
      component: require('./src/content/module/FrontendModules'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['APP_ADMIN'] } ]
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
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['APP_ADMIN'] } ]
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
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['APP_ADMIN'] } ]
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
