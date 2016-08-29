
module.exports = {
  'id': 'core',
  'name': 'Core',
  'description': 'Components for Core module',
  'components': [
    {
      'id': 'dynamicRoleTaskDetail',
      'component': require('./content/task/identityRole/DynamicTaskRoleDetail')
    },
    {
      'id': 'assignedTaskDashboard',
      'type': 'dashboard',
      'span': '6',
      'order': '2',
      'component': require('./content/dashboards/AssignedTaskDashboard')
    },
    {
      'id': 'profileDashboard',
      'type': 'dashboard',
      'span': '5',
      'order': '3',
      'component': require('./content/dashboards/ProfileDashboard')
    }
  ]
};
