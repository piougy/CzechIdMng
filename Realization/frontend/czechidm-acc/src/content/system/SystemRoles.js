import React from 'react';
import { Link } from 'react-router';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Domain, Managers } from 'czechidm-core';
import { RoleSystemManager, SystemMappingManager } from '../../redux';
// import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';
import RoleSystemTable from '../role/RoleSystemTable';

const uiKey = 'system-role-table';
const manager = new RoleSystemManager();
const systemMappingManager = new SystemMappingManager();

/**
 * Table to display roles, assigned to system
 *
 * @author Petr Hanák
 */
export default class SystemRoles extends Advanced.AbstractTableContent {

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
    return 'acc:content.system.roles';
  }

  componentDidMount() {
    this.selectNavigationItems(['sys-systems', 'system-roles']);
  }

  render() {
    const { entityId } = this.props.params.entityId;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', entityId).setSort('created', 'desc');
    return (
      <div className="tab-pane-table-body">
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <Basic.Panel className="no-border last">
          <RoleSystemTable
            columns={['role', 'entityType', 'mapping']}
            uiKey={uiKey}
            identityManager={this.getManager()}
            filterOpened={false}
            forceSearchParameters={forceSearchParameters}
            roleSystemManager={this.roleSystemManager}
            showRowSelection />
        </Basic.Panel>
      </div>
    );
  }
}

SystemRoles.propTypes = {
};

SystemRoles.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(SystemRoles);
