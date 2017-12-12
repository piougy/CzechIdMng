module.exports = {
  'id': 'pwdreset',
  'npmName': 'czechidm-pwdreset',
  'backendId': 'pwdreset',
  'disableable': true,
  'name': 'Password reset module',
  'description': 'Password reset module allows users to reset forgotten passwords.',
  // 'mainStyleFile': 'src/css/main.less',
  'mainRouteFile': 'routes.js',
  'mainComponentDescriptorFile': 'component-descriptor.js',
  'mainLocalePath': 'src/locales/',
  'navigation': {
    'items': [
      {
        'id': 'pwdreset',
        'section': 'main',
        'labelKey': 'pwdreset:navigation.pwdreset',
        'icon': false,
        'order': 11,
        'path': '/password-reset',
        'access': [ { 'type': 'NOT_AUTHENTICATED' } ]
      }
    ]
  }
};
