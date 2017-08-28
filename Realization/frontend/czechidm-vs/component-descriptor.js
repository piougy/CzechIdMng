module.exports = {
  'id': 'vs',
  'name': 'Virtual systems for CzechIdM',
  'description': 'Components for Virtual system module',
  'components': [
    {
      'id': 'vsDashboard',
      'type': 'dashboard',
      'span': '4',
      'order': '4',
      'component': require('./src/content/dashboards/VsDashboard')
    }
  ]
};
