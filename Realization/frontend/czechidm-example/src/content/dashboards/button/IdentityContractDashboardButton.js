import { connect } from 'react-redux';
//
import { Advanced, Managers } from 'czechidm-core';

const identityManager = new Managers.IdentityManager();

/**
 * Quick button to identity detail with contract tab selected.
 *
 * @author Radek Tomiška
 * @since 9.6.0
 */
class IdentityContractDashboardButton extends Advanced.AbstractIdentityDashboardButton {

  getIcon() {
    return 'component:contract';
  }

  isRendered() {
    const { identity, permissions } = this.props;
    //
    return identityManager.canRead(identity, permissions) && Managers.SecurityManager.hasAuthority('IDENTITYCONTRACT_READ');
  }

  getLabel() {
    return this.i18n('content.identity.identityContracts.header');
  }

  getTitle() {
    return this.i18n('content.identity.identityContracts.title');
  }

  onClick() {
    this.context.history.push(`/identity/${ encodeURIComponent(this.getIdentityIdentifier()) }/contracts`);
  }
}

function select(state) {
  return {
    i18nReady: state.config.get('i18nReady'), // required (listen locale is changed)
    userContext: state.security.userContext // required (listen logged user context)
  };
}

export default connect(select)(IdentityContractDashboardButton);
