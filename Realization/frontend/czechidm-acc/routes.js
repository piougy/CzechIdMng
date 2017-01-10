module.exports = {
  module: 'acc',
  component: 'div',
  childRoutes: [
    {
      path: 'systems',
      component: require('./src/content/system/Systems'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
    },
    {
      path: 'system/:entityId/',
      component: require('./src/content/system/System'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ],
      childRoutes: [
        {
          path: 'detail',
          component: require('./src/content/system/SystemContent'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
        },
        {
          path: 'entities',
          component: require('./src/content/system/SystemEntities'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
        },
        {
          path: 'object-classes',
          component: require('./src/content/system/SystemObjectClasses'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
        },
        {
          path: 'entities-handling',
          component: require('./src/content/system/SystemEntitiesHandling'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
        },
        {
          path: 'synchronization-configs',
          component: require('./src/content/system/SystemSynchronizationConfigs'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
        },
        {
          path: 'accounts',
          component: require('./src/content/system/SystemAccounts'),
          access: [ { 'type': 'HAS_ALL_AUTHORITIES', 'authorities': ['SYSTEM_READ', 'ACCOUNT_READ'] } ]
        },
        {
          path: 'connector',
          component: require('./src/content/system/SystemConnector'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
        },
        {
          path: 'schema-object-classes/:objectClassId/detail',
          component: require('./src/content/system/SchemaObjectClassDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
        },
        {
          path: 'schema-object-classes/:objectClassId/new',
          component: require('./src/content/system/SchemaObjectClassDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_WRITE'] } ]
        },
        {
          path: 'system-entities-handling/:entityHandlingId/detail',
          component: require('./src/content/system/SystemEntityHandlingDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
        },
        {
          path: 'system-entities-handling/:entityHandlingId/new',
          component: require('./src/content/system/SystemEntityHandlingDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_WRITE'] } ]
        },
        {
          path: 'schema-attributes/:attributeId/detail',
          component: require('./src/content/system/SchemaAttributeDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
        },
        {
          path: 'schema-attributes/:attributeId/new',
          component: require('./src/content/system/SchemaAttributeDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_WRITE'] } ]
        },
        {
          path: 'schema-attributes-handling/:attributeId/detail',
          component: require('./src/content/system/SchemaAttributeHandlingDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
        },
        {
          path: 'schema-attributes-handling/:attributeId/new',
          component: require('./src/content/system/SchemaAttributeHandlingDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_WRITE'] } ]
        },
        {
          path: 'synchronization-configs/:configId/detail',
          component: require('./src/content/system/SystemSynchronizationConfigDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
        },
        {
          path: 'synchronization-configs/:configId/new',
          component: require('./src/content/system/SystemSynchronizationConfigDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_WRITE'] } ]
        }
      ]
    },
    {
      path: 'system/:entityId/new',
      component: require('./src/content/system/SystemContent'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_WRITE'] } ]
    },
    {
      path: 'identity/:entityId/',
      component: require('czechidm-core/src/content/identity/Identity'),
      childRoutes: [
        {
          path: 'accounts',
          component: require('./src/content/identity/IdentityAccounts')
        }
      ]
    },
    {
      path: 'role/:entityId/',
      component: require('czechidm-core/src/content/role/Role'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ'] } ],
      childRoutes: [
        {
          path: 'systems',
          component: require('./src/content/role/RoleSystems'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ', 'SYSTEM_READ'] } ]
        },
        {
          path: 'systems/:roleSystemId/new',
          component: require('./src/content/role/RoleSystemDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_WRITE', 'SYSTEM_READ'] } ]
        },
        {
          path: 'systems/:roleSystemId/detail',
          component: require('./src/content/role/RoleSystemDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ', 'SYSTEM_READ'] } ]
        },
        {
          path: 'systems/:roleSystemId/attributes/:attributeId/detail',
          component: require('./src/content/role/RoleSystemAttributeDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ', 'SYSTEM_READ'] } ]
        },
        {
          path: 'systems/:roleSystemId/attributes/:attributeId/new',
          component: require('./src/content/role/RoleSystemAttributeDetail'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_WRITE', 'SYSTEM_READ'] } ]
        }
      ]
    },
  ]
};
