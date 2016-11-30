import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import PasswordChangeForm from './PasswordChangeForm';

const RESOURCE_IDM = '0:czechidm';

class PasswordChange extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    this.selectSidebarItem('profile-password');
  }

  render() {
    const { passwordChangeType, userContext, requireOldPassword } = this.props;
    const { entityId } = this.props.params;

    const options = [
      { value: RESOURCE_IDM, niceLabel: 'czechidm (' + entityId + ')'}
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

PasswordChange.propTypes = {
  userContext: PropTypes.object
};
PasswordChange.defaultProps = {
  userContext: null
};

function select(state) {
  return {
    userContext: state.security.userContext
  };
}
export default connect(select)(PasswordChange);
