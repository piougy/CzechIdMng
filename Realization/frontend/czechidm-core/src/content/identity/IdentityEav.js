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
    const { entityId } = this.props.params;
    const { _entity, _permissions } = this.props;
    //
    return (
      <Advanced.EavContent
        uiKey={ uiKey }
        formableManager={ manager }
        entityId={ entityId }
        contentKey={ this.getContentKey() }
        showSaveButton={ manager.canSave(_entity, _permissions) }/>
    );
  }
}

IdentityEav.propTypes = {
  _entity: PropTypes.object,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
IdentityEav.defaultProps = {
  _entity: null,
  _permissions: null
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.params.entityId),
    _permissions: manager.getPermissions(state, null, component.params.entityId)
  };
}

export default connect(select)(IdentityEav);
