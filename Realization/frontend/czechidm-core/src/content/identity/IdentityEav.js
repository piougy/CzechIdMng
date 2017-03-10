import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { IdentityManager } from '../../redux';

const uiKey = 'eav-identity';
const manager = new IdentityManager();

/**
 * Extended identity attributes
 *
 * @author Radek Tomiška
 */
class IdentityEav extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.identity.eav';
  }

  getNavigationKey() {
    return 'profile-eav';
  }

  render() {
    const { userContext } = this.props;
    const canEditMap = manager.canEditMap(userContext);
    const { entityId } = this.props.params;
    //
    return (
      <Advanced.EavContent
        uiKey={uiKey}
        formableManager={manager}
        entityId={entityId}
        contentKey={this.getContentKey()}
        showSaveButton={ canEditMap.get('isSaveEnabled') }/>
    );
  }
}

IdentityEav.propTypes = {
  userContext: PropTypes.object
};
IdentityEav.defaultProps = {
  userContext: null
};

function select(state) {
  return {
    userContext: state.security.userContext
  };
}

export default connect(select)(IdentityEav);
