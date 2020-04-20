import { connect } from 'react-redux';
import uuid from 'uuid';
//
import * as Utils from '../../../utils';
import * as Advanced from '../../../components/advanced';
import SearchParameters from '../../../domain/SearchParameters';
import { IdentityContractManager } from '../../../redux';

const identityContractManager = new IdentityContractManager();

/**
 * Quick button to change identity permissions.
 *
 * @author Radek Tomiška
 * @since 9.6.0
 */
class ChangePermissionDashboardButton extends Advanced.AbstractIdentityDashboardButton {

  componentDidMount() {
    super.componentDidMount();
    //
    const { identity } = this.props;
    this.context.store.dispatch(
      identityContractManager.fetchEntities(
        new SearchParameters(SearchParameters.NAME_AUTOCOMPLETE)
          .setFilter('identity', identity.id)
          .setFilter('validNowOrInFuture', true)
          .setFilter('addPermissions', true),
        `role-identity-contracts-${ identity.id }`
      )
    );
  }

  getIcon() {
    return 'component:identity-roles';
  }

  isRendered() {
    const { permissions, _contracts } = this.props;
    //
    if (Utils.Permission.hasPermission(permissions, 'CHANGEPERMISSION')) {
      return true;
    }
    if (!_contracts || _contracts.length === 0) {
      return false;
    }
    return _contracts.some(c => Utils.Permission.hasPermission(c._permissions, 'CHANGEPERMISSION'));
  }

  getLabel() {
    return this.i18n('content.identity.roles.changePermissions');
  }

  onClick() {
    const { identity } = this.props;
    //
    const uuidId = uuid.v1();
    this.context.history.push(`/role-requests/${uuidId}/new?new=1&applicantId=${identity.id}`);
  }
}

function select(state, component) {
  return {
    i18nReady: state.config.get('i18nReady'), // required
    userContext: state.security.userContext, // required
    _contracts: identityContractManager.getEntities(state, `role-identity-contracts-${ component.identity.id }`)
  };
}

export default connect(select)(ChangePermissionDashboardButton);
