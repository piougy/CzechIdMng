'use strict';

module.exports = {
  'id': 'crt',
  'name': 'Certificates',
  'description': 'Certificates administration. User can send request for certificates, then certificates will be generated and available to download.',
  'navigation': {
    'items': [
      {
        'id': 'certificates-info',
        'parentId': 'user-profile',
        'type': 'TAB',
        'labelKey': 'crt:content.user.sidebar.certificates',
        'order': 25,
        'priority': 0,
        'path': '/user/:userID/certificates'
      }
    ]
  }
}
