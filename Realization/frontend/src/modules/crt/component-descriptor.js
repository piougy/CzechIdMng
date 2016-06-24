'use strict';

import config from '../../../config.json';

module.exports = {
  'id': 'crt',
  'name': 'Certificates',
  'description': 'Components for CRT module',
  'components': [
   {
     'id': 'crt-dashboard',
     'component': require('./content/CrtDashboard'),
     'type': 'dashboard',
     'order': 10
   }
 ]
}
