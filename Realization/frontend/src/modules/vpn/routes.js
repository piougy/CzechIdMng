'use strict';

import VpnRecordProfileDetail from './content/VpnRecordProfileDetail'
import User from '../core/content/user/User';
import config from '../../../config.json';

module.exports = {
  component: 'div',
  childRoutes: [
    {
      path: 'user/:userID/',
      component: User,
      childRoutes: [
        {
          path: 'vpns',
          component: VpnRecordProfileDetail
        }
      ]
    },
    {
      path: 'vpns',
      component: require('./content/Vpns'),
      access: [ { 'type': 'HAS_ANY_ROLE', 'roles': [config.roles.superAdminRole, 'vpnAdmin'] } ]
    }]
  };
