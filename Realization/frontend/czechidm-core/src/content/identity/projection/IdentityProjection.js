import { connect } from 'react-redux';
//
import {
  IdentityManager,
  IdentityProjectionManager,
  DataManager,
  ConfigurationManager,
  CodeListManager
} from '../../../redux';
import AbstractIdentityProjection from './AbstractIdentityProjection';

const identityManager = new IdentityManager();
const identityProjectionManager = new IdentityProjectionManager();
const codeListManager = new CodeListManager();

/**
 * Univarzal form for identity projection..
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
class IdentityProjection extends AbstractIdentityProjection {

}

function select(state, component) {
  const { entityId } = component.match.params;
  const profileUiKey = identityManager.resolveProfileUiKey(entityId);
  const profile = DataManager.getData(state, profileUiKey);
  const identityProjection = identityProjectionManager.getEntity(state, entityId);
  //
  return {
    identityProjection,
    userContext: state.security.userContext,
    showLoading: identityProjectionManager.isShowLoading(state, null, !identityProjection ? entityId : identityProjection.id),
    _imageUrl: profile ? profile.imageUrl : null,
    passwordChangeType: ConfigurationManager.getPublicValue(state, 'idm.pub.core.identity.passwordChange'),
    contractPositions: codeListManager.getCodeList(state, 'contract-position') || []
  };
}

export default connect(select)(IdentityProjection);
