module.exports = {
  module: 'core',
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
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITY_CREATE' ] } ]
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
          path: 'authorities',
          component: require('./src/content/identity/IdentityAuthorities')
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
          component: require('./src/content/identity/IdentityGarantedRoles'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ]
        },
      ]
    },
    {
      path: 'identity/:entityId/revision/:revID',
      component: require('./src/content/identity/AuditDetail')
    },
    {
      path: 'identity/:identityId/identity-contract/:entityId/',
      component: require('./src/content/identity/contract/IdentityContract'),
      childRoutes: [
        {
          path: 'detail',
          component: require('./src/content/identity/contract/IdentityContractContent')
        },
        {
          path: 'eav',
          component: require('./src/content/identity/contract/IdentityContractEav')
        },
        {
          path: 'guarantees',
          component: require('./src/content/identity/contract/IdentityContractGuarantees')
        }
      ]
    },
    {
      path: 'identity/:identityId/identity-contract/:entityId/new',
      component: require('./src/content/identity/contract/IdentityContractContent'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['APP_ADMIN'] } ]
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
      path: 'organizations',
      component: require('./src/content/organization/Organizations'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREETYPE_READ'] } ]
    },
    {
      path: 'tree',
      childRoutes: [
        {
          path: 'nodes',
          component: require('./src/content/tree/node/Nodes'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREENODE_READ'] } ],
        },
        {
          path: 'nodes/:entityId',
          component: require('./src/content/tree/node/Node'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREENODE_READ'] } ],
          childRoutes: [
            {
              path: 'detail',
              component: require('./src/content/tree/node/NodeContent'),
              access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREENODE_READ'] } ]
            },
            {
              path: 'eav',
              component: require('./src/content/tree/node/NodeEav'),
              access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREENODE_READ'] } ]
            },
            {
              path: 'roles',
              component: require('./src/content/tree/node/TreeNodeRoles')
            }
          ]
        },
        {
          path: 'nodes/:entityId/new',
          component: require('./src/content/tree/node/NodeContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREENODE_READ'] } ]
        },
        {
          path: 'types',
          component: require('./src/content/tree/type/Types'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREETYPE_READ'] } ],
        },
        {
          path: 'types/new',
          component: require('./src/content/tree/type/TypeContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREETYPE_CREATE' ] } ]
        },
        {
          path: 'types/:entityId',
          component: require('./src/content/tree/type/TypeContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREETYPE_READ'] } ]
        },
      ]
    },
    {
      path: 'role-catalogues',
      component: require('./src/content/rolecatalogue/RoleCatalogues'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLECATALOGUE_READ'] } ]
    },
    {
      path: 'rolecatalogue/:entityId',
      component: require('./src/content/rolecatalogue/RoleCatalogueContent'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLECATALOGUE_READ'] } ],
    },
    {
      path: 'roles',
      component: require('./src/content/role/Roles'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ]
    },
    {
      path: 'role-requests',
      component: require('./src/content/requestrole/RoleRequests'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLEREQUEST_READ'] } ]
    },
    {
      path: 'role-requests/:entityId/detail',
      component: require('./src/content/requestrole/RoleRequestDetail')
    },
    {
      path: 'role-requests/:entityId/new',
      component: require('./src/content/requestrole/RoleRequestDetail')
    },
    {
      path: 'role/:entityId/',
      component: require('./src/content/role/Role'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ],
      childRoutes: [
        {
          path: 'detail',
          component: require('./src/content/role/RoleContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ]
        },
        {
          path: 'identities',
          component: require('./src/content/role/RoleIdentities'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ]
        },
        {
          path: 'eav',
          component: require('./src/content/role/RoleEav'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ]
        },
        {
          path: 'tree-nodes',
          component: require('./src/content/role/RoleTreeNodes'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ]
        },
        {
          path: 'authorization-policies',
          component: require('./src/content/role/AuthorizationPolicies'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['AUTHORIZATIONPOLICY_READ'] } ]
        }
      ]
    },
    {
      path: 'role/:entityId/new',
      component: require('./src/content/role/RoleContent'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_CREATE'] } ],
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
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['CONFIGURATION_CREATE', 'CONFIGURATION_UPDATE', 'CONFIGURATIONSECURED_READ'] } ]
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
      path: 'forms',
      component: require('./src/content/form/Forms'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['EAVFORMDEFINITIONS_READ'] } ]
    },
    {
      path: 'forms/',
      component: require('./src/content/form/FormRoutes'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['EAVFORMDEFINITIONS_READ'] } ],
      childRoutes: [
        {
          path: ':entityId/detail',
          component: require('./src/content/form/FormDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['EAVFORMDEFINITIONS_READ'] } ]
        },
        {
          path: ':entityId/attributes',
          component: require('./src/content/form/FormAttributes'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['EAVFORMATTRIBUTES_READ'] } ]
        }
      ]
    },
    {
      path: 'forms/attribute/:entityId',
      component: require('./src/content/form/FormAttributeDetail'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['EAVFORMATTRIBUTES_READ'] } ]
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
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['AUDIT_READ'] } ]
        },
        {
          path: 'entities/:entityId/diff',
          component: require('./src/content/audit/AuditDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['AUDIT_READ'] } ]
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
        },
        {
          path: 'templates',
          component: require('./src/content/notification/template/Templates'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATIONTEMPLATE_READ'] } ]
        },
        {
          path: 'templates/:entityId',
          component: require('./src/content/notification/template/TemplateContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['NOTIFICATIONTEMPLATE_READ'] } ]
        }
      ]
    },
    // About site
    {
      path: 'about',
      component: require('./src/content/About'),
      priority: 5
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
      access: [ { type: 'PERMIT_ALL' } ],
      order: 99999 // need to be on end
    },
  ]
};
