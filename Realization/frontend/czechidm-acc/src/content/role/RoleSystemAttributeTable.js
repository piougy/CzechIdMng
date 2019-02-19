import React, { PropTypes } from 'react';
import uuid from 'uuid';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import { Basic, Advanced, Utils } from 'czechidm-core';
import SearchParameters from 'czechidm-core/src/domain/SearchParameters';
import { DataManager, ConfigurationManager } from 'czechidm-core/src/redux';
import { SystemMappingManager, RoleSystemAttributeManager } from '../../redux';
import AttributeMappingStrategyTypeEnum from '../../domain/AttributeMappingStrategyTypeEnum';

let manager = null;
/**
 * Table component to display overridden attributes
 *
 * @author Vít Švanda
 */
export class RoleSystemAttributeTable extends Advanced.AbstractTableContent {

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
    return 'acc:content.role.roleSystemDetail';
  }

  getManager() {
    // Init manager - evaluates if we want to use standard (original) manager or
    // universal request manager (depends on existing of 'requestId' param)
    manager = this.getRequestManager(this.props.params, new RoleSystemAttributeManager());
    return manager;
  }

  getUiKey() {
    return this.props.uiKey;
  }

  /**
  * Redirect to role system detail
  */
  showDetail(entity, add) {
    let entityId = null;
    if (!add) {
      entityId = this.props.isSystemMenu ? entity._embedded.roleSystem.system : entity._embedded.roleSystem.role;
    } else {
      entityId = this.props.params.entityId;
    }
    let roleSystem = this.props.params.roleSystemId;
    if (!roleSystem && entity._embedded && entity._embedded.roleSystem) {
      roleSystem = entity._embedded.roleSystem.id;
    }

    const linkMenu = this.props.isSystemMenu ? `system/${entityId}/roles/${roleSystem}/attributes` : `role/${entityId}/systems/${roleSystem}/attributes`;
    //
    if (add) {
      // When we add new object class, then we need id of role as parametr and use "new" url
      const uuidId = uuid.v1();
      this.context.router.push(`${this.addRequestPrefix(linkMenu, this.props.params)}/${uuidId}/new?new=1&mappingId=${entity.systemMapping}`);
    } else {
      this.context.router.push(`${this.addRequestPrefix(linkMenu, this.props.params)}/${entity.id}/detail`);
    }
  }

  getDefaultSearchParameters() {
    // TODO make this work!!!
    return this.getManager().getDefaultSearchParameters();
  }

  render() {
    const {
      uiKey,
      linkMenu,
      columns,
      forceSearchParameters,
      roleSystem,
      showAddButton,
      readOnly,
      rendered,
      className
    } = this.props;
    //
    if (!rendered) {
      return null;
    }
    if (!manager) {
      return null;
    }
    //
    const _forceSearchParameters = forceSearchParameters || new SearchParameters();
    //
    return (
      <div>
      <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          className={ className }
          manager={ manager }
          forceSearchParameters={ _forceSearchParameters }
          showRowSelection={ !readOnly }
          actions={
            !readOnly
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
                className="btn-xs"
                onClick={this.showDetail.bind(this, roleSystem, true)}
                rendered={showAddButton && !readOnly}>
                <Basic.Icon type="fa" icon="plus"/>
                {' '}
                {this.i18n('button.add')}
              </Basic.Button>
            ]
          }>
          <Advanced.Column
            property=""
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex], false)}/>
                );
              }
            }/>
            <Advanced.ColumnLink
              to={`${linkMenu}/:id/detail`}
              property="name"
              rendered={_.includes(columns, 'name')}
              header={this.i18n('acc:entity.RoleSystemAttribute.name.label')}
              sort />
            <Advanced.Column
              property="idmPropertyName"
              rendered={_.includes(columns, 'idmPropertyName')}
              header={this.i18n('acc:entity.SystemAttributeMapping.idmPropertyName.label')}
              sort/>
            <Advanced.Column
              property="_embedded.roleSystem._embedded.role.name"
              rendered={_.includes(columns, 'role')}
              header={this.i18n('acc:entity.RoleSystem.role')}
              />
            <Advanced.Column
              property="uid"
              rendered={_.includes(columns, 'uid')}
              face="boolean"
              header={this.i18n('acc:entity.SystemAttributeMapping.uid.label')}
              sort/>
            <Advanced.Column
              property="strategyType"
              rendered={_.includes(columns, 'strategyType')}
              width="100px"
              face="enum"
              enumClass={AttributeMappingStrategyTypeEnum}
              header={this.i18n('acc:entity.RoleSystemAttribute.strategyType')}
              sort/>
            <Advanced.Column
              property="entityAttribute"
              rendered={_.includes(columns, 'entityAttribute')}
              face="boolean"
              header={this.i18n('acc:entity.SystemAttributeMapping.entityAttribute')}
              sort/>
            <Advanced.Column
              property="extendedAttribute"
              rendered={_.includes(columns, 'extendedAttribute')}
              face="boolean"
              header={this.i18n('acc:entity.SystemAttributeMapping.extendedAttribute.label')}
              sort/>
            <Advanced.Column
              property="disabledDefaultAttribute"
              rendered={_.includes(columns, 'disabledDefaultAttribute')}
              face="boolean"
              header={this.i18n('acc:entity.RoleSystemAttribute.disabledDefaultAttribute')}
              sort/>
            <Advanced.Column
              property="transformScript"
              rendered={_.includes(columns, 'transformScript')}
              face="boolean"
              header={this.i18n('acc:entity.RoleSystemAttribute.transformScriptTable')}/>
          </Advanced.Table>
      </div>
    );
  }
}

RoleSystemAttributeTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
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

  isSystemMenu: PropTypes.bool,
  /**
   * Rendered
   */
  rendered: PropTypes.bool
};

RoleSystemAttributeTable.defaultProps = {
  columns: ['name', 'idmPropertyName', 'uid', 'entityAttribute', 'strategyType', 'disabledDefaultAttribute', 'extendedAttribute', 'transformScript'],
  filterOpened: false,
  showAddButton: true,
  readOnly: false,
  forceSearchParameters: null,
  rendered: true,
  isSystemMenu: false
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
  };
}

export default connect(select, null, null, { withRef: true })(RoleSystemAttributeTable);
