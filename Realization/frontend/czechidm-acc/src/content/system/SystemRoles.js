import React from 'react';
import { Link } from 'react-router';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Domain, Utils } from 'czechidm-core';
import { RoleSystemManager, SystemManager } from '../../redux';
import RoleSystemTable from '../role/RoleSystemTable';

const uiKey = 'system-roles-table';
const systemManager = new SystemManager();

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
    const { entityId } = this.props.params;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', entityId);
    return (
      <div className="tab-pane-table-body">
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <Basic.Panel className="no-border last">
          <RoleSystemTable
            columns={['role', 'entityType', 'mapping']}
            filterColumns={['roleFilter']}
            uiKey={uiKey}
            roleSystemManager={this.roleSystemManager}
            forceSearchParameters={forceSearchParameters}
            showRowSelection
            showAddButton={false}
            entityId={entityId} />
        </Basic.Panel>
      </div>
    );
  }
}

SystemRoles.propTypes = {
};

SystemRoles.defaultProps = {
};

function select(state, component) {
  return {
    system: Utils.Entity.getEntity(state, systemManager.getEntityType(), component.params.entityId),
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
    _searchParameters: Utils.Ui.getSearchParameters(state, uiKey)
  };
}

export default connect(select)(SystemRoles);
