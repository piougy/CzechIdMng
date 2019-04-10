import { connect } from 'react-redux';
import uuid from 'uuid';
//
import * as Utils from '../../../utils';
import * as Advanced from '../../../components/advanced';

/**
 * Quick button to change identity permissions.
 *
 * @author Radek Tomiška
 * @since 9.6.0
 */
class ChangePermissionDashboardButton extends Advanced.AbstractIdentityDashboardButton {

  constructor(props, context) {
    super(props, context);
  }

  getIcon() {
    return 'component:identity-roles';
  }

  isRendered() {
    const { permissions } = this.props;
    //
    return Utils.Permission.hasPermission(permissions, 'CHANGEPERMISSION');
  }

  getLabel() {
    return this.i18n('content.identity.roles.changePermissions');
  }

  onClick() {
    const { identity } = this.props;
    //
    const uuidId = uuid.v1();
    this.context.router.push(`/role-requests/${uuidId}/new?new=1&applicantId=${identity.id}`);
  }
}

function select(state) {
  return {
    i18nReady: state.config.get('i18nReady'), // required
    userContext: state.security.userContext, // required
  };
}

export default connect(select)(ChangePermissionDashboardButton);
