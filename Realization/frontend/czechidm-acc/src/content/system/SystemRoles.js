import React from 'react';
import Helmet from 'react-helmet';
import { Link } from 'react-router';
//
import { Basic, Advanced, Domain, Managers } from 'czechidm-core';
import { RoleSystemManager, SystemMappingManager } from '../../redux';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';

const uiKey = 'system-role-table';
const manager = new RoleSystemManager();
const roleManager = new Managers.RoleManager();
const systemMappingManager = new SystemMappingManager();

/**
 * Table to display roles, assigned to system
 *
 * @author Petr Hanák
 */
export default class SystemRoles extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
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

  showDetail(entity) {
    const roleId = entity.role;
    this.context.router.push(`role/${roleId}/detail`);
  }

  _getSystemMappingLink(roleSystem) {
    return (
      <Link to={`/system/${roleSystem._embedded.system.id}/mappings/${roleSystem._embedded.systemMapping.id}/detail`} >{systemMappingManager.getNiceLabel(roleSystem._embedded.systemMapping)}</Link>
    );
  }

  render() {
    const { entityId } = this.props.params;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', entityId).setSort('created', 'desc');
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          <Advanced.Table
            ref="table"
            uiKey={uiKey}
            manager={this.getManager()}
            forceSearchParameters={forceSearchParameters}
            showRowSelection={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])}
            actions={
              Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])
              ?
              [{ value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }]
              :
              null
            }
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row>
                    <Basic.Col lg={ 8 }>
                      <Advanced.Filter.SelectBox
                        ref="roleId"
                        manager={ roleManager }
                        placeholder={this.i18n('acc:content.system.roles.filter.role')}/>
                    </Basic.Col>
                    <Basic.Col lg={ 4 } className="text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                    </Basic.Col>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
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
            <Advanced.Column
              property="_embedded.role.name"
              header={this.i18n('core:entity.Role._type')}
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
            <Advanced.Column
              property="_embedded.systemMapping.entityType"
              header={this.i18n('acc:entity.SystemEntity.entityType')}
              sort face="enum"
              enumClass={SystemEntityTypeEnum} />
            <Advanced.Column
              property="systemMapping"
              header={this.i18n('acc:entity.RoleSystem.systemMapping')}
              cell={
                ({ rowIndex, data }) => {
                  return this._getSystemMappingLink(data[rowIndex]);
                }
              }/>
          </Advanced.Table>
        </Basic.Panel>
      </div>
    );
  }
}

SystemRoles.propTypes = {
};

SystemRoles.defaultProps = {
};
