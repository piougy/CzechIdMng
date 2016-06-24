'use strict';

import config from '../../../config.json';
import DefaultTaskDetail from './content/task/DefaultTaskDetail';
import RoleApprovalTaskDetail from './content/task/RoleApprovalTaskDetail';

module.exports = {
  'id': 'core',
  'name': 'Core',
  'description': 'Components for Core module',
  'components': [
    {
      'id': 'defaultApprovalTaskDetail',
      'component': DefaultTaskDetail
    },
    {
      'id': 'roleApprovalTaskDetail',
      'component': RoleApprovalTaskDetail
    },
    {
      'id': 'assigned-task-dashboard',
      'component': require('./content/dashboards/AssignedTaskDashboard'),
      'type': 'dashboard',
      'order': 50,
      'span': 6
    }
  ]
}
