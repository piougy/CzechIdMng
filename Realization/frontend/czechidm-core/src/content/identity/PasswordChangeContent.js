import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import PasswordChangeForm from './PasswordChangeForm';
import ComponentService from '../../services/ComponentService';
import ConfigLoader from '../../utils/ConfigLoader';

const IDM_NAME = ConfigLoader.getConfig('app.name', 'CzechIdM');
const RESOURCE_IDM = `0:${IDM_NAME}`;

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
    const { entityId } = this.props.params;
    //
    const options = [
      { value: RESOURCE_IDM, niceLabel: `${IDM_NAME} (${entityId})`}
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
