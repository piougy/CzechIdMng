'use strict';

import config from '../../../config.json';

module.exports = {
  'id': 'core',
  'name': 'Core',
  'description': 'Components for Core module',
  'components': [
    {
      'id': 'assigned-task-dashboard',
      'component': require('./content/dashboards/AssignedTaskDashboard'),
      'type': 'dashboard',
      'order': 50,
      'span': 6
    }
  ]
}
