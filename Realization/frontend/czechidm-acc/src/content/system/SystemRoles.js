import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Domain, Managers, Utils } from 'czechidm-core';
import { RoleSystemManager } from '../../redux';
import uuid from 'uuid';

const uiKey = 'system-role-table';
const manager = new RoleSystemManager();
const roleManager = new Managers.RoleManager();

/**
 * Table to display roles, assigned to system
 *
 * @author Petr Hanák
 */
class SystemRoles extends Advanced.AbstractTableContent {

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
                      ref="roleSystem"
                      placeholder={this.i18n('content.roles.filter.text.placeholder')}/>
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
              }
              />
            <Advanced.Column
              property="_embedded.role.description"
              header={this.i18n('core:entity.Role.description')}
              />
            <Advanced.Column
              face="bool"
              property="_embedded.role.disabled"
              header={this.i18n('core:entity.Role.disabled')}
              />
          </Advanced.Table>
        </Basic.Panel>
      </div>
    );
  }
}

SystemRoles.propTypes = {
  role: PropTypes.object,
  _showLoading: PropTypes.bool,
};

SystemRoles.defaultProps = {
  role: null,
  _showLoading: false,
};

function select(state, component) {
  return {
    role: Utils.Entity.getEntity(state, roleManager.getEntityType(), component.params.entityId),
  };
}

export default connect(select)(SystemRoles);
