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
          component: require('./src/content/identity/PasswordChangeRoute')
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
        },
        {
          path: 'subordinates',
          component: require('./src/content/identity/IdentitySubordinates')
        },
        {
          path: 'eav',
          component: require('./src/content/identity/IdentityEav')
        },
        {
          path: 'garanted-roles',
          component: require('./src/content/identity/IdentityGarantedRoles')
        },
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
      path: 'role-catalogues',
      component: require('./src/content/rolecatalogue/RoleCatalogues'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ]
    },
    {
      path: 'rolecatalogue/:entityId',
      component: require('./src/content/rolecatalogue/RoleCatalogueContent'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_WRITE'] } ],
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
        {
          path: 'identities',
          component: require('./src/content/role/RoleIdentities')
        }
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
      path: 'modules',
      component: require('./src/content/module/ModuleRoutes'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['MODULE_READ'] } ],
      childRoutes: [
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
          path: 'entity-event-processors',
          component: require('./src/content/module/EntityEventProcessors'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['MODULE_READ'] } ]
        }
      ]
    },
    {
      path: 'scheduler',
      component: require('./src/content/scheduler/SchedulerRoutes'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SCHEDULER_READ'] } ],
      childRoutes: [
        {
          path: 'running-tasks',
          component: require('./src/content/scheduler/RunningTasks'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SCHEDULER_READ'] } ]
        },
        {
          path: 'schedule-tasks',
          component: require('./src/content/scheduler/ScheduleTasks'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SCHEDULER_READ'] } ]
        },
        {
          path: 'all-tasks',
          component: require('./src/content/scheduler/AllTasks'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SCHEDULER_READ'] } ]
        }
      ]
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
      path: 'scripts',
      component: require('./src/content/script/Scripts'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SCRIPT_READ'] } ]
    },
    {
      path: 'scripts/:entityId',
      component: require('./src/content/script/ScriptContent'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SCRIPT_READ'] } ]
    },
    {
      path: 'password-policies',
      component: require('./src/content/passwordpolicy/PasswordPolicies'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['PASSWORDPOLICY_READ'] } ]
    },
    {
      path: 'password-policies/',
      component: require('./src/content/passwordpolicy/PasswordPolicyRoutes'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['PASSWORDPOLICY_READ'] } ],
      childRoutes: [
        {
          path: ':entityId',
          component: require('./src/content/passwordpolicy/PasswordPolicyBasic'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['PASSWORDPOLICY_READ'] } ]
        },
        {
          path: ':entityId/advanced',
          component: require('./src/content/passwordpolicy/PasswordPolicyAdvanced'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['PASSWORDPOLICY_READ'] } ]
        },
        {
          path: ':entityId/characters',
          component: require('./src/content/passwordpolicy/PasswordPolicyCharacters'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['PASSWORDPOLICY_READ'] } ]
        }
      ]
    },
    {
      path: 'audit/',
      component: 'div',
      childRoutes: [
        {
          path: 'entities',
          component: require('./src/content/audit/AuditContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['AUDIT_READ'] } ]
        },
        {
          path: 'entities/:entityId/diff/:revID',
          component: require('./src/content/audit/AuditDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATION_READ'] } ]
        },
        {
          path: 'entities/:entityId/diff',
          component: require('./src/content/audit/AuditDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATION_READ'] } ]
        }
      ]
    },
    {
      path: 'notification/',
      component: 'div',
      childRoutes: [
        {
          path: 'notifications',
          component: require('./src/content/notification/Notifications'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATION_READ'] } ]
        },
        {
          path: 'notification/:entityId',
          component: require('./src/content/notification/NotificationContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATION_READ'] } ]
        },
        {
          path: 'emails',
          component: require('./src/content/notification/email/Emails'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATION_READ'] } ]
        },
        {
          path: 'emails/:entityId',
          component: require('./src/content/notification/email/EmailContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATION_READ'] } ]
        },
        {
          path: 'websockets',
          component: require('./src/content/notification/websocket/Websockets'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATION_READ'] } ]
        },
        {
          path: 'websockets/:entityId',
          component: require('./src/content/notification/websocket/WebsocketContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATION_READ'] } ]
        },
        {
          path: 'configurations',
          component: require('./src/content/notification/configuration/NotificationConfigurations'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATIONCONFIGURATION_READ'] } ]
        }
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
