
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
      'id': 'role-tree-node-task-detail',
      'component': require('./src/content/task/roleTreeNode/AutomaticRoleTaskDetail')
    },
    {
      'id': 'assigned-task-dashboard',
      'type': 'dashboard',
      'span': '8',
      'order': '3',
      'component': require('./src/content/dashboards/AssignedTaskDashboard')
    },
    {
      'id': 'profile-dashboard',
      'type': 'dashboard',
      'span': '4',
      'order': '1',
      'component': require('./src/content/dashboards/ProfileDashboard')
    },
    {
      'id': 'password-change-content',
      'priority': 0,
      'component': require('./src/content/identity/PasswordChangeContent')
    }
  ]
};
