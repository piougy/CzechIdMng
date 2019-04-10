import { connect } from 'react-redux';
//
import { IdentityManager } from '../../../redux';
import * as Advanced from '../../../components/advanced';

const identityManager = new IdentityManager();

/**
 * Quick button to identity detail.
 *
 * @author Radek Tomiška
 * @since 9.6.0
 */
class IdentityDetailDashboardButton extends Advanced.AbstractIdentityDashboardButton {

  constructor(props, context) {
    super(props, context);
  }

  getIcon() {
    return 'fa:angle-double-right';
  }

  isRendered() {
    const { identity, permissions } = this.props;
    //
    return identityManager.canRead(identity, permissions);
  }

  getLabel() {
    return this.i18n('component.advanced.IdentityInfo.link.detail.label');
  }

  onClick() {
    this.context.router.push(`/identity/${encodeURIComponent(this.getIdentityIdentifier())}/profile`);
  }
}

function select(state) {
  return {
    i18nReady: state.config.get('i18nReady'), // required
    userContext: state.security.userContext // required
  };
}

export default connect(select)(IdentityDetailDashboardButton);
