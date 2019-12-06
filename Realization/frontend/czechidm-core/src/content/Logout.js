import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../components/basic';
import { SecurityManager } from '../redux';

const securityManager = new SecurityManager();

/**
 * Logout page
 *
 * @author Radek Tomiška
 */
class Logout extends Basic.AbstractContent {

  UNSAFE_componentWillMount() {
    // logout immediately, when component will mount
    this.context.store.dispatch(securityManager.logout(() => {
      this.context.history.replace('/login');
    }));
  }

  render() {
    return <Basic.Loading isStatic showLoading />;
  }

}

export default connect()(Logout);
