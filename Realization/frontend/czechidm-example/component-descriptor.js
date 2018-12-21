module.exports = {
  'id': 'example',
  'name': 'Example',
  'description': 'Components for Example module',
  'components': [
    {
      'id': 'exampleDashboard',
      'type': 'dashboard',
      'order': '400',
      'component': require('./src/content/dashboards/ExampleDashboard')
    },
    {
      'id': 'priority-select-form-value',
      'type': 'form-attribute-renderer',
      'persistentType': 'INT',
      'faceType': 'PRIORITY-SELECT',
      'component': require('./src/components/PrioritySelectFormAttributeRenderer'),
      'labelKey': 'Priority radio select'
    }
  ]
};
