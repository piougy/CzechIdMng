import React, { PropTypes } from 'react';
import uuid from 'uuid';
import { connect } from 'react-redux';
import { Link } from 'react-router';
import _ from 'lodash';
//
import { Basic, Advanced, Managers, Utils } from 'czechidm-core';
import { SystemInfo } from '../../components/SystemInfo/SystemInfo.js';
import SearchParameters from 'czechidm-core/src/domain/SearchParameters';
import { DataManager, SecurityManager, ConfigurationManager, RoleManager } from 'czechidm-core/src/redux';
import { SystemMappingManager, SystemManager } from '../../redux';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';

const roleManager = new RoleManager();
const systemManager = new SystemManager();
/**
 * Table component to display roles, assigned to system
 *
 * @author Petr Hanák
 */
export class RoleSystemTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: props.filterOpened
    };
    this.dataManager = new DataManager();
    this.systemMappingManager = new SystemMappingManager();
    this.configurationManager = new ConfigurationManager();
  }

  getContentKey() {
    return 'acc:content.role.systems';
  }

  getManager() {
    return this.props.roleSystemManager;
  }

  getUiKey() {
    return this.props.uiKey;
  }

  /**
  * Redirect to role system detail
  */
  showDetail(entity) {
    const roleId = entity.role;
    const roleSystemId = entity.id;
    // TODO handle add button
    // TODO make add button universal
    //
    if (roleSystemId === undefined) {
      // When we add new object class, then we need id of role as parametr and use "new" url
      const uuidId = uuid.v1();
      this.context.router.push(`/role/${roleId}/systems/${uuidId}/new?new=1`);
    } else {
      this.context.router.push(`role/${roleId}/systems/${roleSystemId}/detail`);
    }
  }

  addRoleSystemConnection() {
    // TODO Works only in RoleSystem tab, make it work in SystemRole!!!
    const roleId = this.props.entityId;
    const uuidId = uuid.v1();
    this.context.router.push(`/role/${roleId}/systems/${uuidId}/new?new=1`);
  }

  getDefaultSearchParameters() {
    // TODO make this work!!!
    return this.getManager().getDefaultSearchParameters();
  }

  _getSystemMappingLink(roleSystem) {
    return (
      <Link to={`/system/${roleSystem._embedded.system.id}/mappings/${roleSystem._embedded.systemMapping.id}/detail`} >{this.systemMappingManager.getNiceLabel(roleSystem._embedded.systemMapping)}</Link>
    );
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      text: null,
      treeNodeId: null
    }, () => {
      this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
    });
  }

  systemInfoCard({ rowIndex, data }) {
    return (
      <SystemInfo
        entityIdentifier={ data[rowIndex]._embedded.system.id }
        entity={ data[rowIndex]._embedded.system }
        face="popover" />
    );
  }

  render() {
    const {
      uiKey,
      roleSystemManager,
      columns,
      forceSearchParameters,
      showAddButton,
      showDetailButton,
      deleteEnabled,
      showRowSelection,
      rendered,
      treeType,
      filterOpened,
      showFilter,
      filterColumns
    } = this.props;
    //
    if (!rendered) {
      return null;
    }
    //
    let _forceSearchParameters = forceSearchParameters || new SearchParameters();

    return (
      <div>
      <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={roleSystemManager}
          showRowSelection={showRowSelection && (SecurityManager.hasAuthority('IDENTITY_UPDATE') || SecurityManager.hasAuthority('IDENTITY_DELETE'))}
          forceSearchParameters={_forceSearchParameters}
          filterOpened={ filterOpened }
          showFilter={ showFilter }
          filterColumns={ filterColumns }
          actions={
            Managers.SecurityManager.hasAnyAuthority(['ROLE_UPDATE'])
            ?
            [{ value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }]
            :
            null
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                type="submit"
                className="btn-xs"
                onClick={this.addRoleSystemConnection.bind(this, {})}
                rendered={showAddButton && SecurityManager.hasAuthority('IDENTITY_CREATE')}
                icon="fa:plus">
                {this.i18n('button.add')}
              </Basic.Button>
            ]
          }
          filter={
            <Filter
              ref="filterForm"
              onSubmit={ this.useFilter.bind(this) }
              onCancel={ this.cancelFilter.bind(this) }
              roleSystemManager = { roleSystemManager }
              filterColumns={ filterColumns }
              forceSearchParameters={ forceSearchParameters } />
          }
          _searchParameters={ this.getSearchParameters() }>
          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex])}/>
                );
              }
            }
            sort={ false }
            rendered={ showDetailButton }/>
            <Advanced.Column
              property="_embedded.systemMapping.entityType"
              header={this.i18n('acc:entity.SystemEntity.entityType')}
              rendered={_.includes(columns, 'entityType')}
              face="enum"
              enumClass={SystemEntityTypeEnum}
              sort
              sortProperty="systemMapping.entityType" />
            <Advanced.Column
              property="_embedded.role.name"
              header={this.i18n('core:entity.Role._type')}
              rendered={_.includes(columns, 'role')}
              sort
              sortProperty="role.name"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <Advanced.RoleInfo
                      entityIdentifier={ data[rowIndex]._embedded.role.id }
                      entity={ data[rowIndex]._embedded.role }
                      face="popover" />
                  );
                }
              }/>
            <Advanced.ColumnLink
              to="/system/:_target/detail"
              target="_embedded.system.id"
              access={{ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ']}}
              property="_embedded.system.name"
              header={this.i18n('acc:entity.RoleSystem.system')}
              rendered={_.includes(columns, 'system')}
              sort
              sortProperty="system.name"
              cell={this.systemInfoCard.bind(this)} />
            <Advanced.Column
              property="systemMapping"
              header={this.i18n('acc:entity.RoleSystem.systemMapping')}
              rendered={_.includes(columns, 'mapping')}
              cell={
                ({ rowIndex, data }) => {
                  return this._getSystemMappingLink(data[rowIndex]);
                }
              }/>
        </Advanced.Table>
      </div>
    );
  }
}

RoleSystemTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  roleSystemManager: PropTypes.object.isRequired,
  /**
   * Rendered columns - see table columns above
   */
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Button for create user will be shown
   */
  showAddButton: PropTypes.bool,
  /**
   * Detail button will be shown
   */
  showDetailButton: PropTypes.bool,
  /**
   * Show filter
   */
  showFilter: PropTypes.bool,
  /**
   * Table supports delete identities
   */
  deleteEnabled: PropTypes.bool,
  /**
   * Enable row selection - checkbox in first cell
   */
  showRowSelection: PropTypes.bool,
  /**
   * Rendered
   */
  rendered: PropTypes.bool,
  /**
   * Filter tree type structure - given id ur default - false
   * @deprecated Remove after better tree type - node filter component
   */
  treeType: PropTypes.oneOfType([PropTypes.bool, PropTypes.string]),
};

RoleSystemTable.defaultProps = {
  columns: ['entityType', 'role', 'system', 'mapping'],
  filterOpened: true,
  showAddButton: true,
  showDetailButton: true,
  showFilter: true,
  deleteEnabled: false,
  showRowSelection: false,
  forceSearchParameters: null,
  rendered: true,
  treeType: false
};

function select(state, component) {
  return {
    roleSystem: Utils.Entity.getEntity(state, component.roleSystemManager.getEntityType(), component.entityId),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    deleteEnabled: ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.core.identity.delete')
  };
}

export default connect(select, null, null, { withRef: true })(RoleSystemTable);

/**
 * Table filter component
 *
 * @author Radek Tomiška
 */
class Filter extends Advanced.Filter {

  render() {
    const {
      onSubmit,
      onCancel,
      filterOpened,
      filterColumns,
    } = this.props;
    //
    return (
      <Advanced.Filter
      onSubmit={ onSubmit }
      filterOpened={ filterOpened }
      >
        <Basic.AbstractForm ref="filterForm">
          <Basic.Row className="last">
            <Basic.Col lg={ 4 } rendered={_.includes(filterColumns, 'roleFilter')}>
            <Advanced.Filter.SelectBox
                    ref="roleId"
                    manager={ roleManager }
                    placeholder={ this.i18n('acc:content.role.systems.filter.roleSystem.placeholder') }/>
            </Basic.Col>
            <Basic.Col lg={ 4 } rendered={_.includes(filterColumns, 'systemFilter')}>
            <Advanced.Filter.SelectBox
                    ref="systemId"
                    manager={ systemManager }
                    placeholder={ this.i18n('acc:content.role.systems.filter.roleSystem.placeholder') }/>
            </Basic.Col>
            <Basic.Col lg={ 8 } className="text-right">
              <Advanced.Filter.FilterButtons cancelFilter={ onCancel }/>
            </Basic.Col>
          </Basic.Row>
        </Basic.AbstractForm>
      </Advanced.Filter>
    );
  }
}

Filter.defaultProps = {
  filterColumns: ['roleFilter', 'systemFilter'],
};
