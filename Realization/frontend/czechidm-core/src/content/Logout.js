import React from 'react';
import * as Basic from '../components/basic';
import { SecurityManager } from '../redux';

const securityManager = new SecurityManager();

export default class Logout extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  componentWillMount() {
    this.context.store.dispatch(securityManager.logout(() => {
      this.context.router.replace('/login');
    }));
  }

  render() {
    return <Basic.Loading isStatic showLoading />;
  }
}
