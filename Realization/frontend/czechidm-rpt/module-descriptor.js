module.exports = {
  'id': 'rpt',
  'npmName': 'czechidm-rpt',
  'backendId': 'rpt',
  'name': 'Report module.',
  'description': 'Contains support for reports.',
  'mainRouteFile': 'routes.js',
  'mainComponentDescriptorFile': 'component-descriptor.js',
  'mainLocalePath': 'src/locales/',
  'navigation': {
    'items': [
      {
        'id': 'rpt-reports',
        'type': 'DYNAMIC',
        'section': 'main',
        'icon': 'fa:line-chart',
        'labelKey': 'rpt:content.reports.label',
        'titleKey': 'rpt:content.reports.title',
        'order': 1800,
        'path': '/rpt/reports',
        'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['REPORT_READ'] } ]
      }
    ]
  }
};
