
module.exports = {
  'id': 'core',
  'name': 'Core',
  'description': 'Components for Core module',
  'components': [
    {
      'id': 'dynamicRoleTaskDetail',
      'component': require('./src/content/task/identityRole/DynamicTaskRoleDetail')
    },
    {
      'id': 'assignedTaskDashboard',
      'type': 'dashboard',
      'span': '6',
      'order': '3',
      'component': require('./src/content/dashboards/AssignedTaskDashboard')
    },
    {
      'id': 'profileDashboard',
      'type': 'dashboard',
      'span': '5',
      'order': '1',
      'component': require('./src/content/dashboards/ProfileDashboard')
    }
  ]
};
