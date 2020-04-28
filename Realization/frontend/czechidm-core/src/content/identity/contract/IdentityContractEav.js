import PropTypes from 'prop-types';
import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { IdentityContractManager } from '../../../redux';

const manager = new IdentityContractManager();

/**
 * Extended identity contract's attributes
 *
 * @author Radek Tomiška
 */
class IdentityContractEav extends Basic.AbstractContent {

  componentDidMount() {
    this.selectSidebarItem('identity-contract-eav');
  }

  getContentKey() {
    return 'content.identity-contract.eav';
  }

  render() {
    const { entityId} = this.props.match.params;
    //
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />
        <Advanced.EavContent
          formableManager={ manager }
          entityId={ entityId}
          contentKey={ this.getContentKey() }
          showSaveButton/>
      </Basic.Div>
    );
  }
}

IdentityContractEav.propTypes = {
  _entity: PropTypes.object,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
IdentityContractEav.defaultProps = {
  _entity: null,
  _permissions: null
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.match.params.entityId),
    _permissions: manager.getPermissions(state, null, component.match.params.entityId)
  };
}

export default connect(select)(IdentityContractEav);
