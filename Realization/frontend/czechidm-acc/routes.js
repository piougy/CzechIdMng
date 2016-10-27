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
          path: 'accounts',
          component: require('./src/content/system/SystemAccounts'),
          access: [ { 'type': 'HAS_ALL_AUTHORITIES', 'authorities': ['SYSTEM_READ', 'ACCOUNT_READ'] } ]
        },
        {
          path: 'connector',
          component: require('./src/content/system/SystemConnector'),
          access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
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
      ]
    },
  ]
};
