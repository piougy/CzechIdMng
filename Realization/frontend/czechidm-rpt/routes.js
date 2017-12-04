module.exports = {
  module: 'rpt',
  childRoutes: [
    {
      path: '/rpt/reports',
      component: require('./src/content/report/Reports'),
      access: [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['REPORT_READ'] } ]
    }
  ]
};
