import { connect } from 'react-redux';
//
import * as Advanced from '../../../components/advanced';
import * as Utils from '../../../utils';
import { IdentityManager, ConfigurationManager } from '../../../redux';

const identityManager = new IdentityManager();

/**
 * Quick button to change identity password.
 *
 * @author Radek Tomiška
 * @since 9.6.0
 */
class PasswordChangeDashboardButton extends Advanced.AbstractIdentityDashboardButton {

  getIcon() {
    return 'component:password';
  }

  isRendered() {
    const { passwordChangeType, permissions } = this.props;
    //
    return identityManager.canChangePassword(passwordChangeType, permissions)
      || Utils.Permission.hasPermission(permissions, 'PASSWORDRESET');
  }

  getLabel() {
    return this.i18n('content.password.change.header');
  }

  onClick() {
    this.context.history.push(`/identity/${encodeURIComponent(this.getIdentityIdentifier())}/password/change`);
  }
}

function select(state) {
  return {
    i18nReady: state.config.get('i18nReady'), // required
    userContext: state.security.userContext, // required
    passwordChangeType: ConfigurationManager.getPublicValue(state, 'idm.pub.core.identity.passwordChange')
  };
}

export default connect(select)(PasswordChangeDashboardButton);
