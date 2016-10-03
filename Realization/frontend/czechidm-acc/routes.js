module.exports = {
  module: 'acc',
  component: 'div',
  childRoutes: [
    {
      path: 'systems',
      component: require('./src/content/system/Systems'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ' ] } ]
    },
    {
      path: 'system/:entityId',
      component: require('./src/content/system/SystemContent'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ'] } ]
    },
    {
      path: 'user/:userID/',
      component: require('czechidm-core/src/content/user/User'),
      childRoutes: [
        {
          path: 'accounts',
          component: require('./src/content/user/Accounts'),
          access: [ { 'type': 'DENY_ALL', 'authorities': ['SYSTEM_READ' ] } ]
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
