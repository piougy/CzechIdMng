module.exports = {
  'id': 'example',
  'name': 'Example',
  'description': 'Components for Example module',
  'components': [
    {
      'id': 'exampleDashboard',
      'type': 'dashboard',
      'span': '4',
      'order': '4',
      'component': require('./src/content/dashboards/ExampleDashboard')
    },
    {
      'id': 'priority-select-form-value',
      'type': 'form-value',
      'persistentType': 'INT',
      'faceType': 'PRIORITY-SELECT',
      'component': require('./src/components/PrioritySelectFormValue'),
      'labelKey': 'Priority radio select'
    }
  ]
};
