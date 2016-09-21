import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { RoleManager } from '../../redux';
import * as Advanced from '../../components/advanced';


const roleManager = new RoleManager();

class Role extends Basic.AbstractContent {

  componentDidMount() {
  }

  componentDidUpdate() {
  }

  render() {
    const { role } = this.props;

    return (
      <div>
        <Helmet title={this.i18n('navigation.menu.profile')} />

        <Basic.PageHeader>
          {roleManager.getNiceLabel(role)} <small> {this.i18n('content.roles.edit.header')}</small>
        </Basic.PageHeader>

        <Basic.Panel>
          <Basic.PanelHeader text={<span>{roleManager.getNiceLabel(role)} <small> Detail role</small></span>} className="hidden">
          </Basic.PanelHeader>
          <div className="tab-vertical clearfix">
            <Advanced.TabPanel parentId="role-tabs" params={this.props.params}>
              {this.props.children}
            </Advanced.TabPanel>
          </div>
        </Basic.Panel>
      </div>
    );
  }
}

Role.propTypes = {
  role: PropTypes.object,
  userContext: PropTypes.object,
  selectedSidebarItem: PropTypes.string
};
Role.defaultProps = {
  identity: null,
  userContext: null,
  selectedSidebarItem: null
};

function select(state, component) {
  const { entityId } = component.params;
  const selectedNavigationItems = state.layout.get('selectedNavigationItems');
  const selectedSidebarItem = (selectedNavigationItems.length > 1) ? selectedNavigationItems[1] : null;
  return {
    role: roleManager.getEntity(state, entityId),
    userContext: state.security.userContext,
    selectedSidebarItem
  };
}

export default connect(select)(Role);
