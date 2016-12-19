import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import PasswordChangeForm from './PasswordChangeForm';
import ComponentService from '../../services/ComponentService';

const RESOURCE_IDM = '0:CzechIdM';

class PasswordChangeContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.componentService = new ComponentService();
  }

  componentDidMount() {
    this.selectSidebarItem('profile-password');
  }

  render() {
    const { passwordChangeType, userContext, requireOldPassword } = this.props;
    const { entityId } = this.props.params;

    const options = [
      { value: RESOURCE_IDM, niceLabel: 'CzechIdM (' + entityId + ')'}
    ];

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
    userContext: state.security.userContext
  };
}
export default connect(select)(PasswordChangeContent);
