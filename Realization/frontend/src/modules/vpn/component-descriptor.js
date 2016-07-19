

import config from '../../../config.json'
import VpnApprovalTaskDetail from './content/task/VpnApprovalTaskDetail';

module.exports = {
  'id': 'vpn',
  'name': 'VPN',
  'description': 'Components for VPN module',
  'components': [
    {
     'id': 'vpnApprovalTaskDetail',
     'component': VpnApprovalTaskDetail
   },
   {
     'id': 'vpn-dashboard',
     'component': require('./content/VpnDashboard'),
     'type': 'dashboard',
     'order': 20
   }
 ]
}
