import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import * as Basic from '../../../components/basic';
import { IdentityContractManager } from '../../../redux';
import * as Advanced from '../../../components/advanced';
import OrganizationPosition from '../OrganizationPosition';

const manager = new IdentityContractManager();

/**
 * Identity contract tabs - entry point
 *
 * @author Radek Tomiška
 */
class IdentityContract extends Basic.AbstractContent {

  componentDidMount() {
    this._selectNavigationItem();
    const { entityId } = this.props.match.params;
    //
    this.context.store.dispatch(manager.fetchEntityIfNeeded(entityId));
  }

  componentDidUpdate() {
    this._selectNavigationItem();
  }

  /**
   * Lookot: getNavigationKey cannot be used -> profile vs users main tab
   */
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
    const { entity, showLoading, match } = this.props;
    const {params} = match;
    const paramsResult = _.merge(params, { controlledBySlices: !!(entity && entity.controlledBySlices)});
    match.params = paramsResult;

    return (
      <div>
        <Basic.PageHeader showLoading={!entity && showLoading}>
          {manager.getNiceLabel(entity)} <small> {this.i18n('content.identity-contract.detail.header')}</small>
        </Basic.PageHeader>

        <OrganizationPosition identity={ params.identityId }/>
        <Basic.Alert
          rendered={!!(entity && entity.controlledBySlices)}
          level="info"
          text={this.i18n('content.identity-contract.detail.alert.controlledBySlices')}/>

        <Advanced.TabPanel parentId="profile-contracts" match={match}>
          {this.getRoutes()}
        </Advanced.TabPanel>
      </div>
    );
  }
}

IdentityContract.propTypes = {
  entity: PropTypes.object,
  userContext: PropTypes.object,
  showLoading: PropTypes.bool
};
IdentityContract.defaultProps = {
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

export default connect(select)(IdentityContract);
