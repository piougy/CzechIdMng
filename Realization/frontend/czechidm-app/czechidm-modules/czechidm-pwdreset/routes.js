module.exports = {
  module: 'pwdreset',
  component: 'div',
  childRoutes: [
    {
      path: 'password-reset',
      component: require('./src/content/PasswordReset'),
      access: [ { type: 'PERMIT_ALL' } ]
    },
    {
      path: 'password-reset/verify',
      component: require('./src/content/VerifyReset'),
      access: [ { type: 'PERMIT_ALL' } ]
    }
  ]
};
