
import config from '../../../config.json';


module.exports = {
  'id': 'vpn',
  'name': 'VPN',
  'description': 'VPN administration.',
  'navigation': {
    'items': [
      {
        'id': 'vpn-info',
        'parentId': 'user-profile',
        'type': 'TAB',
        'labelKey': 'vpn:content.user.sidebar.vpns',
        'order': 26,
        'priority': 0,
        'path': '/user/:userID/vpns'
      },
      {
        'id': 'vpns',
        'labelKey': 'vpn:navigation.menu.vpns.label',
        'titleKey': 'vpn:navigation.menu.vpns.title',
        'icon': 'fa:key',
        'order': 50,
        'path': '/vpns',
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [config.authorities.superAdminAuthority, 'vpnAdmin'] } ]
      },
    ]
  }
}
