import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../../components/basic';
import { ContractSliceManager } from '../../../redux';
import * as Advanced from '../../../components/advanced';
import OrganizationPosition from '../OrganizationPosition';

const manager = new ContractSliceManager();

/**
 * Identity contract tabs - entry point
 *
 * @author Radek Tomiška
 */
class ContractSlice extends Basic.AbstractContent {

  componentDidMount() {
    this._selectNavigationItem();
    const { entityId } = this.props.match.params;
    //
    this.context.store.dispatch(manager.fetchEntityIfNeeded(entityId));
  }

  componentDidUpdate() {
    this._selectNavigationItem();
  }

  _selectNavigationItem() {
    const { identityId } = this.props.match.params;
    const { userContext } = this.props;
    if (identityId === userContext.username) {
      this.selectNavigationItems(['identity-profile', null]);
    } else {
      this.selectNavigationItems(['identities', null]);
    }
  }

  render() {
    const { identityId } = this.props.match.params;
    const { entity, showLoading } = this.props;
    //
    return (
      <Basic.Div>
        <Advanced.DetailHeader
          icon="component:contract-slice"
          entity={ entity }
          showLoading={ !entity && showLoading }
          to={ entity ? `/identity/${ encodeURIComponent(identityId) }/contracts` : null }>
          { manager.getNiceLabel(entity) } <small> { this.i18n('content.contract-slice.detail.header') }</small>
        </Advanced.DetailHeader>

        <OrganizationPosition identity={ identityId }/>

        <Advanced.TabPanel parentId="identity-contract-slices" match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}

ContractSlice.propTypes = {
  entity: PropTypes.object,
  userContext: PropTypes.object,
  showLoading: PropTypes.bool
};
ContractSlice.defaultProps = {
  entity: null,
  userContext: null,
  showLoading: false
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    entity: manager.getEntity(state, entityId),
    userContext: state.security.userContext,
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(ContractSlice);
