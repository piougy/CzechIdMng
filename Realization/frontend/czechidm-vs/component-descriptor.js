module.exports = {
  'id': 'vs',
  'name': 'Virtual systems for CzechIdM',
  'description': 'Components for Virtual system module',
  'components': [
    {
      'id': 'vsDashboard',
      'type': 'dashboard',
      'span': '8',
      'order': '4',
      'component': require('./src/content/dashboards/VsDashboard')
    },
    {
      'id': 'vs-request-info',
      'type': 'entity-info',
      'entityType': ['vs-request', 'VsRequest'],
      'component': require('./src/components/advanced/VsRequestInfo/VsRequestInfo').default,
      'manager': require('./src/redux').VsRequestManager
    },
  ]
};
