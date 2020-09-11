module.exports = {
  id: 'rpt',
  components: [
    {
      id: 'report-info',
      type: 'entity-info',
      entityType: ['rptReport', 'RptReport', 'RptReportDto'],
      component: require('./src/components/ReportInfo/ReportInfo').default,
      manager: require('./src/redux').ReportManager
    },
    {
      id: 'report-icon',
      type: 'icon',
      entityType: ['report', 'reports'],
      component: 'fa:line-chart'
    }
  ]
};
