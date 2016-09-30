import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import * as Advanced from '../../components/advanced';

const identityManager = new IdentityManager();

class User extends Basic.AbstractContent {

  componentDidMount() {
    this._selectNavigationItem();
    const { entityId } = this.props.params;
    //
    this.context.store.dispatch(identityManager.fetchEntityIfNeeded(entityId));
  }

  componentDidUpdate() {
    this._selectNavigationItem();
  }

  _selectNavigationItem() {
    const { entityId } = this.props.params;
    const { userContext, selectedSidebarItem } = this.props;
    if (entityId === userContext.username && selectedSidebarItem !== 'profile-subordinates') {
      this.selectNavigationItems(['user-profile', null]);
    } else {
      this.selectNavigationItems(['user-subordinates', null]);
    }
  }

  render() {
    const { identity } = this.props;

    return (
      <div>
        <Helmet title={this.i18n('navigation.menu.profile')} />

        <Basic.PageHeader>
          {identityManager.getNiceLabel(identity)} <small> {this.i18n('content.user.profile.userDetail')}</small>
        </Basic.PageHeader>

        <Basic.Panel>
          <Basic.PanelHeader text={<span>{identityManager.getNiceLabel(identity)} <small> Detail uživatele</small></span>} className="hidden">
          </Basic.PanelHeader>
          <div className="tab-vertical clearfix">
            <Advanced.TabPanel parentId="user-profile" params={this.props.params}>
              {this.props.children}
            </Advanced.TabPanel>
          </div>
        </Basic.Panel>
      </div>
    );
  }
}

User.propTypes = {
  identity: PropTypes.object,
  userContext: PropTypes.object,
  selectedSidebarItem: PropTypes.string
};
User.defaultProps = {
  identity: null,
  userContext: null,
  selectedSidebarItem: null
};

function select(state, component) {
  const { entityId } = component.params;
  const selectedNavigationItems = state.layout.get('selectedNavigationItems');
  const selectedSidebarItem = (selectedNavigationItems.length > 1) ? selectedNavigationItems[1] : null;
  return {
    identity: identityManager.getEntity(state, entityId),
    userContext: state.security.userContext,
    selectedSidebarItem
  };
}

export default connect(select)(User);
