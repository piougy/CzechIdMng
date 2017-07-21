module.exports = {
  'id': 'core',
  'npmName': 'czechidm-core',
  'name': 'Core',
  'disableable': false,
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
        'labelKey': 'navigation.menu.profile.label',
        'titleKey': 'navigation.menu.profile.title',
        'icon': 'user',
        'iconColor': '#428BCA',
        'order': 10,
        'priority': 0,
        'path': '/identity/:loggedUsername/profile',
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITY_READ'] } ],
        'items': [
          {
            'id': 'profile-personal',
            'type': 'TAB',
            'labelKey': 'content.identity.sidebar.profile',
            'order': 10,
            'priority': 0,
            'path': '/identity/:entityId/profile',
            'icon': 'user',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITY_READ'] } ]
          },
          {
            'id': 'profile-eav',
            'type': 'TAB',
            'labelKey': 'content.identity.eav.title',
            'order': 11,
            'priority': 0,
            'path': '/identity/:entityId/eav',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITY_READ'] } ]
          },
          {
            'id': 'profile-password',
            'type': 'TAB',
            'labelKey': 'content.identity.sidebar.password',
            'order': 20,
            'path': '/identity/:entityId/password',
            'icon': 'lock',
            'conditions': [
              'todo: eval( canPasswordChange ...)'
            ],
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITY_PASSWORDCHANGE'] } ]
          },
          {
            'id': 'profile-roles',
            'type': 'TAB',
            'labelKey': 'content.identity.sidebar.roles',
            'order': 30,
            'path': '/identity/:entityId/roles',
            'icon': 'fa:universal-access',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITYROLE_READ'] } ]
          },
          {
            'id': 'profile-authorities',
            'type': 'TAB',
            'labelKey': 'content.identity.authorities.label',
            'titleKey': 'content.identity.authorities.title',
            'order': 40,
            'path': '/identity/:entityId/authorities',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['AUTHORIZATIONPOLICY_READ'] } ]
          },
          {
            'id': 'profile-contracts',
            'type': 'TAB',
            'labelKey': 'entity.IdentityContract._type',
            'order': 50,
            'path': '/identity/:entityId/contracts',
            'icon': 'fa:building',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITYCONTRACT_READ'] } ],
            'items': [
              {
                'id': 'identity-contract-detail',
                'type': 'TAB',
                'labelKey': 'content.identity-contract.detail.label',
                'order': 10,
                'path': '/identity/:identityId/identity-contract/:entityId/detail',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITYCONTRACT_READ'] } ]
              },
              {
                'id': 'identity-contract-eav',
                'type': 'TAB',
                'labelKey': 'content.identity-contract.eav.label',
                'order': 20,
                'path': '/identity/:identityId/identity-contract/:entityId/eav',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITYCONTRACT_READ'] } ],
              },
              {
                'id': 'identity-contract-guarantees',
                'type': 'TAB',
                'labelKey': 'content.identity-contract.guarantees.label',
                'order': 30,
                'path': '/identity/:identityId/identity-contract/:entityId/guarantees',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITYCONTRACT_READ'] } ],
              }
            ]
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
                'icon': 'user',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['AUDIT_READ'] } ]
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
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITY_READ'] } ]
          },
          {
            'id': 'profile-garanted-roles',
            'type': 'TAB',
            'labelKey': 'content.identity.garanted-roles.title',
            'order': 70,
            'path': '/identity/:entityId/garanted-roles',
            'icon': 'fa:universal-access',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ]
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
        'id': 'organizations',
        'labelKey': 'content.organizations.label',
        'titleKey': 'content.organizations.title',
        'icon': 'tree-deciduous',
        'order': 1020,
        'iconColor': '#419641',
        'path': '/organizations',
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREENODE_READ'] } ]
      },
      {
        'id': 'profile-system-separator',
        'type': 'SEPARATOR',
        'access': [ { 'type': 'DENY_ALL' } ], // RT: PoC only - design is ugly ...
        'labelKey': 'navigation.menu.separator.system',
        'order': 999
      },
      {
        'id': 'roles-menu',
        'labelKey': 'content.roles.menu-title',
        'titleKey': 'content.roles.menu-title',
        'icon': 'fa:universal-access',
        'iconColor': '#eb9316',
        'order': 1030,
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ],
        'path': '/roles',
        'items': [
          {
            'id': 'roles',
            'type': 'DYNAMIC',
            'labelKey': 'content.roles.header',
            'titleKey': 'content.roles.title',
            'icon': 'fa:universal-access',
            'iconColor': '#eb9316',
            'order': 10,
            'path': '/roles',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ],
            'items': [
              {
                'id': 'role-detail',
                'type': 'TAB',
                'labelKey': 'content.roles.tabs.basic',
                'order': 100,
                'path': '/role/:entityId/detail',
                'icon': 'fa:newspaper-o'
              },
              {
                'id': 'role-eav',
                'type': 'TAB',
                'labelKey': 'content.role.eav.title',
                'order': 110,
                'priority': 0,
                'path': '/role/:entityId/eav',
              },
              {
                'id': 'role-authorization-policies',
                'type': 'TAB',
                'labelKey': 'content.role.authorization-policies.label',
                'titleKey': 'content.role.authorization-policies.title',
                'order': 200,
                'path': '/role/:entityId/authorization-policies',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['AUTHORIZATIONPOLICY_READ'] } ]
              },
              {
                'id': 'role-tree-nodes',
                'type': 'TAB',
                'labelKey': 'content.role.tree-nodes.label',
                'titleKey': 'content.role.tree-nodes.title',
                'order': 300,
                'path': '/role/:entityId/tree-nodes',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLETREENODE_READ'] } ]
              },
              {
                'id': 'role-identities',
                'type': 'TAB',
                'labelKey': 'content.role.identities.title',
                'order': 400,
                'path': '/role/:entityId/identities',
                'icon': 'fa:group'
              }
            ]
          },
          {
            'id': 'role-requests',
            'labelKey': 'content.roleRequests.title',
            'order': 20,
            'path': '/role-requests',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLEREQUEST_READ'] } ]
          }
        ]
      },
      {
        'id': 'audit',
        'labelKey': 'content.audit.title',
        'titleKey': 'content.audit.title',
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
            'id': 'audits',
            'labelKey': 'content.audit.title',
            'order': 40,
            'path': '/audit/entities',
            'icon': 'eye-open',
            'access': [
              {
                'type': 'HAS_ANY_AUTHORITY',
                'authorities': ['AUDIT_READ']
              }
            ],
            'items': [
              {
                'id': 'audit-identities',
                'labelKey': 'content.audit.title-identities',
                'order': 42,
                'path': '/audit/identities',
                'icon': '',
                'type': 'TAB',
                'access': [
                  {
                    'type': 'HAS_ANY_AUTHORITY',
                    'authorities': ['AUDIT_READ']
                  }
                ]
              },
              {
                'id': 'audit-entities',
                'labelKey': 'content.audit.title-entities',
                'order': 40,
                'path': '/audit/entities',
                'icon': '',
                'type': 'TAB',
                'access': [
                  {
                    'type': 'HAS_ANY_AUTHORITY',
                    'authorities': ['AUDIT_READ']
                  }
                ]
              }
            ]
          },
          {
            'id': 'audit-notification',
            'labelKey': 'navigation.menu.notifications.label',
            'titleKey': 'navigation.menu.notifications.title',
            'icon': 'fa:envelope',
            'order': 1910,
            'path': '/notification/notifications',
            'access': [
              {
                'type': 'DENY_ALL', // TODO: fix issue with mets menu - collapsing different submenu is broken
                'authorities': ['NOTIFICATION_READ']
              }
            ],
          }
        ]
      },
      {
        'id': 'notification',
        'labelKey': 'navigation.menu.notifications.label',
        'titleKey': 'navigation.menu.notifications.title',
        'icon': 'fa:envelope',
        'order': 1910,
        'path': '/notification/notifications',
        'access': [
          {
            'type': 'HAS_ANY_AUTHORITY',
            'authorities': ['NOTIFICATION_READ']
          }
        ],
        'items': [
          {
            'id': 'notification-notifications',
            'labelKey': 'content.notifications.label',
            'titleKey': 'content.notifications.title',
            'order': 30,
            'path': '/notification/notifications',
            'icon': 'fa:envelope',
            'access': [
              {
                'type': 'HAS_ANY_AUTHORITY',
                'authorities': ['NOTIFICATION_READ']
              }
            ]
          },
          {
            'id': 'notification-emails',
            'labelKey': 'content.emails.title',
            'order': 35,
            'path': '/notification/emails',
            'icon': 'fa:envelope-o',
            'access': [
              {
                'type': 'HAS_ANY_AUTHORITY',
                'authorities': ['NOTIFICATION_READ']
              }
            ]
          },
          {
            'id': 'notification-websockets',
            'labelKey': 'content.websockets.title',
            'order': 100,
            'path': '/notification/websockets',
            'access': [
              {
                'type': 'HAS_ANY_AUTHORITY',
                'authorities': ['NOTIFICATION_READ']
              }
            ]
          },
          {
            'id': 'notification-sms',
            'labelKey': 'content.sms.title',
            'order': 200,
            'path': '/notification/sms',
            'icon': 'fa:commenting-o',
            'access': [
              {
                'type': 'HAS_ANY_AUTHORITY',
                'authorities': ['NOTIFICATION_READ']
              }
            ]
          },
          {
            'id': 'notification-templates',
            'labelKey': 'content.notificationTemplate.title',
            'icon': 'fa:envelope-square',
            'order': 900,
            'path': '/notification/templates',
            'access': [
              {
                'type': 'HAS_ANY_AUTHORITY',
                'authorities': ['NOTIFICATIONTEMPLATE_READ']
              }
            ]
          },
          {
            'id': 'notification-configurations',
            'labelKey': 'content.notificationConfigurations.label',
            'titleKey': 'content.notificationConfigurations.title',
            'order': 1000,
            'path': '/notification/configurations',
            'icon': 'cog',
            'access': [
              {
                'type': 'HAS_ANY_AUTHORITY',
                'authorities': ['NOTIFICATIONCONFIGURATION_READ']
              }
            ]
          },
        ]
      },
      {
        'id': 'system',
        'labelKey': 'navigation.menu.system',
        'titleKey': 'navigation.menu.system',
        'icon': 'cog',
        'order': 2000,
        'path': '/configurations',
        'iconColor': '#c12e2a',
        'access': [
          {
            'type': 'HAS_ANY_AUTHORITY',
            'authorities': [
              'TREENODE_UPDATE', 'CONFIGURATION_READ', 'MODULE_READ',
              'SCHEDULER_READ', 'EAVFORMDEFINITIONS_READ', 'PASSWORDPOLICY_READ', 'SCRIPT_READ', 'ROLECATALOGUE_UPDATE'
            ]
          }
        ],
        'items': [
          {
            'id': 'system-configuration',
            'labelKey': 'navigation.menu.configuration',
            'icon': 'cog',
            'order': 10,
            'path': '/configurations',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['CONFIGURATION_READ'] } ]
          },
          {
            'id': 'modules',
            'labelKey': 'content.system.modules.title',
            'order': 50,
            'path': '/modules/be-modules',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['MODULE_READ'] } ],
            'icon': 'fa:puzzle-piece',
            'items': [
              {
                'id': 'fe-modules',
                'labelKey': 'content.system.fe-modules.title',
                'order': 20,
                'path': '/modules/fe-modules',
                'icon': '',
                'type': 'TAB',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['APP_ADMIN'] } ]
              },
              {
                'id': 'be-modules',
                'labelKey': 'content.system.be-modules.title',
                'order': 10,
                'path': '/modules/be-modules',
                'icon': '',
                'type': 'TAB',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['MODULE_READ'] } ]
              },
              {
                'id': 'entity-event-processors',
                'labelKey': 'content.system.entity-event-processors.title',
                'order': 30,
                'path': '/modules/entity-event-processors',
                'icon': '',
                'type': 'TAB',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['MODULE_READ'] } ]
              }
            ]
          },
          {
            'id': 'scheduler',
            'labelKey': 'content.scheduler.title',
            'order': 55,
            'path': '/scheduler/running-tasks',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SCHEDULER_READ'] } ],
            'icon': 'fa:calendar-times-o',
            'items': [
              {
                'id': 'scheduler-running-tasks',
                'labelKey': 'content.scheduler.running-tasks.title',
                'order': 10,
                'path': '/scheduler/running-tasks',
                'icon': '',
                'type': 'TAB',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SCHEDULER_READ'] } ]
              },
              {
                'id': 'scheduler-schedule-tasks',
                'labelKey': 'content.scheduler.schedule-tasks.title',
                'order': 20,
                'path': '/scheduler/schedule-tasks',
                'icon': '',
                'type': 'TAB',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SCHEDULER_READ'] } ]
              },
              {
                'id': 'scheduler-all-tasks',
                'labelKey': 'content.scheduler.all-tasks.title',
                'order': 30,
                'path': '/scheduler/all-tasks',
                'icon': '',
                'type': 'TAB',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SCHEDULER_READ'] } ]
              }
            ]
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
            'id': 'tree',
            'labelKey': 'content.tree.header',
            'titleKey': 'content.tree.title',
            'icon': 'tree-deciduous',
            'order': 80,
            'iconColor': '#419641',
            'path': '/tree/nodes',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREENODE_UPDATE'] } ],
            'items': [
              {
                'id': 'tree-nodes',
                'labelKey': 'content.tree.nodes.title',
                'order': 15,
                'icon': 'apple',
                'path': '/tree/nodes',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREENODE_UPDATE'] } ],
                'items': [
                  {
                    'id': 'tree-node-detail',
                    'type': 'TAB',
                    'labelKey': 'content.tree.node.detail.label',
                    'order': 10,
                    'path': '/tree/nodes/:entityId/detail',
                    'icon': 'fa:newspaper-o'
                  },
                  {
                    'id': 'tree-node-eav',
                    'type': 'TAB',
                    'labelKey': 'content.tree.node.eav.title',
                    'order': 20,
                    'path': '/tree/nodes/:entityId/eav',
                  },
                  {
                    'id': 'tree-node-roles',
                    'type': 'TAB',
                    'labelKey': 'content.tree.node.roles.label',
                    'titleKey': 'content.tree.node.roles.title',
                    'order': 30,
                    'path': '/tree/nodes/:entityId/roles',
                    'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLETREENODE_READ'] } ]
                  }
                ]
              },
              {
                'id': 'tree-types',
                'labelKey': 'content.tree.types.title',
                'titleKey': 'content.tree.types.title',
                'order': 10,
                'icon': 'tree-deciduous',
                'path': '/tree/types',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREETYPE_UPDATE'] } ]
              }
            ]
          },
          {
            'id': 'role-catalogues',
            'labelKey': 'content.roleCatalogues.header',
            'titleKey': 'content.roleCatalogues.title',
            'icon': 'fa:list-alt',
            'iconColor': '#dad727',
            'order': 100,
            'path': '/role-catalogues',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLECATALOGUE_READ'] } ],
          },
          {
            'id': 'password-policies',
            'labelKey': 'content.passwordPolicies.header',
            'titleKey': 'content.passwordPolicies.title',
            'icon': 'fa:key',
            'iconColor': '#ff0000',
            'order': 60,
            'path': '/password-policies',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['PASSWORDPOLICY_READ'] } ],
            'items': [
              {
                'id': 'password-policies-basic',
                'labelKey': 'content.passwordPolicies.basic.title',
                'order': 10,
                'path': '/password-policies/:entityId',
                'icon': '',
                'type': 'TAB',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['PASSWORDPOLICY_READ'] } ]
              },
              {
                'id': 'password-policies-advanced',
                'labelKey': 'content.passwordPolicies.advanced.title',
                'order': 15,
                'path': '/password-policies/:entityId/advanced',
                'icon': '',
                'type': 'TAB',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['PASSWORDPOLICY_READ'] } ]
              },
              {
                'id': 'password-policies-characters',
                'labelKey': 'content.passwordPolicies.characters.title',
                'order': 20,
                'path': '/password-policies/:entityId/characters',
                'icon': '',
                'type': 'TAB',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['PASSWORDPOLICY_READ'] } ]
              }
            ]
          },
          {
            'id': 'scripts',
            'labelKey': 'content.scripts.header',
            'titleKey': 'content.scripts.title',
            'icon': 'fa:clone',
            'iconColor': '#272fd8',
            'order': 30,
            'path': '/scripts',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SCRIPT_READ'] } ],
          },
          {
            'id': 'forms',
            'labelKey': 'content.formDefinitions.header',
            'titleKey': 'content.formDefinitions.title',
            'icon': 'fa:wpforms',
            'iconColor': '#000000',
            'order': 40,
            'path': '/forms',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['EAVFORMDEFINITIONS_READ'] } ],
            'items': [
              {
                'id': 'forms-detail',
                'labelKey': 'content.formDefinitions.detail.title',
                'order': 10,
                'path': '/forms/:entityId/detail',
                'icon': '',
                'type': 'TAB',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['EAVFORMDEFINITIONS_READ'] } ]
              },
              {
                'id': 'forms-attributes',
                'labelKey': 'content.formDefinitions.attributes.title',
                'order': 100,
                'path': '/forms/:entityId/attributes',
                'icon': '',
                'type': 'TAB',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['EAVFORMATTRIBUTES_READ'] } ]
              }
            ]
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
