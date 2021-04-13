module.exports = {
  id: 'example',
  name: 'Example',
  description: 'Components for Example module',
  components: [
    {
      id: 'exampleDashboard',
      type: 'dashboard',
      order: '400',
      component: require('./src/content/dashboards/ExampleDashboard').default
    },
    {
      id: 'identity-contract-dashboard-button',
      type: 'identity-dashboard-button',
      order: 150,
      component: require('./src/content/dashboards/button/IdentityContractDashboardButton').default
    },
    {
      id: 'priority-select-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'INT',
      faceType: 'PRIORITY-SELECT',
      component: require('./src/components/PrioritySelectFormAttributeRenderer').default,
      labelKey: 'Priority radio select'
    },
    {
      id: 'custom-roles-icon',
      type: 'icon',
      entityType: [ 'my-roles', 'roles'],
      component: require('./src/components/ExampleRoleIcon').default,
      priority: 1
    },
    {
      id: 'marcel-icon',
      type: 'icon',
      entityType: [ 'marcel'],
      component: require('./src/components/MarcelIcon').default
    }
  ]
};
