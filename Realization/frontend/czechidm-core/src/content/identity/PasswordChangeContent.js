import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import PasswordChangeForm from './PasswordChangeForm';
import ComponentService from '../../services/ComponentService';
import ConfigLoader from '../../utils/ConfigLoader';
import { ConfigurationManager } from '../../redux';

const IDM_NAME = ConfigLoader.getConfig('app.name', 'CzechIdM');
const RESOURCE_IDM = `0:${IDM_NAME}`;

class PasswordChangeContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.componentService = new ComponentService();
  }

  componentDidMount() {
    this.selectSidebarItem('profile-password');
  }

  render() {
    const { passwordChangeType, userContext, requireOldPassword, allowedPasswordChangeForIdm } = this.props;
    const { entityId } = this.props.params;

    const options = [ ];
    if (allowedPasswordChangeForIdm) {
      options.push({ value: RESOURCE_IDM, niceLabel: `${IDM_NAME} (${entityId})`});
    }

    return (
      <PasswordChangeForm
        userContext={userContext}
        accountOptions={options}
        entityId={entityId}
        passwordChangeType={passwordChangeType}
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
    userContext: state.security.userContext,
    allowedPasswordChangeForIdm: ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.core.identity.passwordChange.idm.enabled')
  };
}
export default connect(select)(PasswordChangeContent);
