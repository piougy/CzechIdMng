import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Domain, Managers, Utils } from 'czechidm-core';
import { RoleSystemManager, SystemMappingManager } from '../../redux';
import RoleSystemTable from '../role/RoleSystemTable';

const uiKey = 'role-systems-table';
const manager = new RoleSystemManager();
const roleManager = new Managers.RoleManager();


class RoleSystems extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.roleSystemManager = new RoleSystemManager();
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.role.systems';
  }

  componentDidMount() {
    this.selectNavigationItems(['roles', 'role-systems']);
  }

  render() {
    const { entityId } = this.props.params.entityId;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('roleId', entityId);
    return (
      <div className="tab-pane-table-body">
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <Basic.Panel className="no-border last">
          <RoleSystemTable
            columns={['system', 'entityType', 'mapping', 'add']}
            uiKey={uiKey}
            identityManager={this.getManager()}
            filterOpened={false}
            forceSearchParameters={forceSearchParameters}
            showRowSelection
            roleSystemManager={this.roleSystemManager}
            entityId = {this.props.params.entityId} />
        </Basic.Panel>
      </div>
    );
  }
}

RoleSystems.propTypes = {
  role: PropTypes.object,
  _showLoading: PropTypes.bool,
};
RoleSystems.defaultProps = {
  role: null,
  _showLoading: false,
};

function select(state, component) {
  return {
    role: Utils.Entity.getEntity(state, roleManager.getEntityType(), component.params.entityId),
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(RoleSystems);
