import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import PasswordChangeForm from './PasswordChangeForm';
import ComponentService from '../../services/ComponentService';
import ConfigLoader from '../../utils/ConfigLoader';
import { IdentityManager } from '../../redux';

const IDM_NAME = ConfigLoader.getConfig('app.name', 'CzechIdM');
const RESOURCE_IDM = `0:${IDM_NAME}`;

const identityManager = new IdentityManager();

/**
 * Password change on the identity detail - overidable content
 *
 * @author Ondřej Kopr
 */
class PasswordChangeContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.componentService = new ComponentService();
  }

  render() {
    const { userContext, requireOldPassword } = this.props;
    const { entityId } = this.props.match.params;
    //
    const identity = identityManager.getEntity(this.context.store.getState(), entityId);
    const options = [
      { value: RESOURCE_IDM, niceLabel: `${IDM_NAME}${ identity ? ` (${ identity.username })` : '' }` }
    ];
    return (
      <PasswordChangeForm
        userContext={userContext}
        accountOptions={options}
        entityId={entityId}
        requireOldPassword={requireOldPassword}/>
    );
  }
}

PasswordChangeContent.propTypes = {
  userContext: PropTypes.object
};
PasswordChangeContent.defaultProps = {
  userContext: null
};

function select(state) {
  return {
    userContext: state.security.userContext
  };
}
export default connect(select)(PasswordChangeContent);
