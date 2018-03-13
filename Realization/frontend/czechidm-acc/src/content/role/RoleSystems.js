import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Domain, Managers, Utils } from 'czechidm-core';
import { RoleSystemManager } from '../../redux';
import RoleSystemTable from '../role/RoleSystemTable';

const uiKey = 'role-systems-table';
const roleManager = new Managers.RoleManager();


class RoleSystems extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.roleSystemManager = new RoleSystemManager();
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
    const { entityId } = this.props.params;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('roleId', entityId);
    return (
      <div className="tab-pane-table-body">
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <Basic.Panel className="no-border last">
          <RoleSystemTable
            columns={['system', 'entityType', 'mapping']}
            filterColumns={['systemFilter']}
            uiKey={uiKey}
            roleSystemManager={this.roleSystemManager}
            forceSearchParameters={forceSearchParameters}
            showRowSelection
            entityId={entityId} />
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
    _searchParameters: Utils.Ui.getSearchParameters(state, uiKey)
  };
}

export default connect(select)(RoleSystems);
