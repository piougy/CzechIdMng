

import User from '../core/content/user/User';
import CrtIdentityDetail from './content/CrtIdentityDetail';
import VpnRecordProfileDetail from '../vpn/content/VpnRecordProfileDetail'

module.exports = {
  path: 'user/:userID/',
  component: User,
  childRoutes: [
    {
      path: 'certificates',
      component: CrtIdentityDetail
    }
  ]
};
