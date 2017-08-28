module.exports = {
  'id': 'vs',
  'npmName': 'czechidm-vs',
  'backendId': 'vs',
  'name': 'Virtual system for CzechIdM',
  'description': 'Virtual system module for CzechIdM. Provides system connector for connect CzechIdM systems. Virtual systems are used in situations, when connector operations (create/update/delete accounts) have to be realizate manually. Basicly virtual system for every active connector operation creates task for realizater (admin of real system).',
  // 'mainStyleFile': 'src/css/main.less',
  'mainRouteFile': 'routes.js',
  'mainComponentDescriptorFile': 'component-descriptor.js',
  'mainLocalePath': 'src/locales/',
  'navigation': {
    'items': [
      {
        'id': 'vs-main-menu',
        'labelKey': 'vs:content.virtuals.label',
        'titleKey': 'vs:content.virtuals.title',
        'icon': 'link',
        'iconColor': '#008AFF',
        'order': 1101,
        'items': [
          {
            'id': 'vs-requests',
            'type': 'DYNAMIC',
            'section': 'main',
            'labelKey': 'vs:content.vs-requests.label',
            'titleKey': 'vs:content.vs-requests.title',
            'order': 20,
            'path': '/vs/requests',
            'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['VSREQUEST_READ'] } ],
            'items': [
              {
                'id': 'vs-request-detail',
                'type': 'TAB',
                'labelKey': 'vs:content.vs-request.detail.basic',
                'order': 10,
                'path': '/vs/request/:entityId/detail',
                'icon': 'gift',
                'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['VSREQUEST_READ'] } ]
              },
            ]
          }
        ]
      }
    ]
  }
};
