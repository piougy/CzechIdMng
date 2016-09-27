module.exports = {
  'id': 'acc',
  'npmName': 'czechidm-acc',
  'name': 'Account managment',
  'description': 'Module for account managment',
  // 'mainStyleFile': 'src/css/main.less',
  // 'mainRouteFile': 'routes.js',
  'mainLocalePath': 'src/locales/',
  'navigation': {
    'items': [
      {
        'id': 'personal-accounts',
        'parentId': 'user-profile',
        'type': 'TAB',
        'labelKey': 'acc:content.user.accounts',
        'order': 15,
        'priority': 0,
        'path': '/user/:userID/accounts',
        'icon': 'link'
      }
    ]
  }
};
