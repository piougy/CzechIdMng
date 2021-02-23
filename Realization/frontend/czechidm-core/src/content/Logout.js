import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../components/basic';
import { SecurityManager, ConfigurationManager } from '../redux';

const securityManager = new SecurityManager();

/**
 * Logout page
 *
 * @author Radek Tomiška
 * @author Roman Kučera
 */
class Logout extends Basic.AbstractContent {

  UNSAFE_componentWillMount() {
    // logout immediately, when component will mount
    const { casEnabled, casUrl, idmUrl, casLogoutSuffix } = this.props;

    this.context.store.dispatch(securityManager.logout(() => {
      if (casEnabled) {
        window.location.replace(`${casUrl}${casLogoutSuffix}${idmUrl}`);
      } else {
        this.context.history.replace('/login');
      }
    }));
  }

  render() {
    return <Basic.Loading isStatic showLoading />;
  }

}

function select(state) {
  return {
    casEnabled: ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.core.cas.sso.enabled', false),
    casUrl: ConfigurationManager.getValue(state, 'idm.pub.core.cas.url'),
    idmUrl: ConfigurationManager.getValue(state, 'idm.pub.core.cas.idm-url'),
    casLogoutSuffix: ConfigurationManager.getValue(state, 'idm.pub.core.cas.logout-suffix')
  };
}

export default connect(select)(Logout);
