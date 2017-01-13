import React, { PropTypes } from 'react';
import { Link } from 'react-router';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Domain, Managers, Utils } from 'czechidm-core';
import { RoleSystemManager, SystemMappingManager } from '../../redux';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';
import uuid from 'uuid';

const uiKey = 'role-systems-table';
const manager = new RoleSystemManager();
const roleManager = new Managers.RoleManager();
const systemMappingManager = new SystemMappingManager();


class RoleSystems extends Basic.AbstractTableContent {

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
    return 'acc:content.role.systems';
  }

  componentDidMount() {
    this.selectNavigationItems(['roles', 'role-systems']);
  }

  showDetail(entity, add) {
    const roleId = this.props.params.entityId;
    if (add) {
      // When we add new object class, then we need id of role as parametr and use "new" url
      const uuidId = uuid.v1();
      this.context.router.push(`/role/${roleId}/systems/${uuidId}/new?new=1`);
    } else {
      this.context.router.push(`role/${roleId}/systems/${entity.id}/detail`);
    }
  }

  render() {
    const { entityId } = this.props.params;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('roleId', entityId);
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
            showRowSelection={Managers.SecurityManager.hasAnyAuthority(['ROLE_WRITE'])}
            actions={
              Managers.SecurityManager.hasAnyAuthority(['ROLE_WRITE'])
              ?
              [{ value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }]
              :
              null
            }
            buttons={
              [
                <Basic.Button level="success" key="add_button" className="btn-xs" onClick={this.showDetail.bind(this, null, true)} rendered={Managers.SecurityManager.hasAnyAuthority(['ROLE_WRITE'])}>
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
            <Advanced.Column
              property="systemMapping.entityType"
              header={this.i18n('acc:entity.SystemEntity.entityType')}
              sort face="enum"
              enumClass={SystemEntityTypeEnum} />
            <Advanced.ColumnLink
              to="/system/:_target/detail"
              target="system.id"
              access={{ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ']}}
              property="system.name"
              header={this.i18n('acc:entity.RoleSystem.system')}
              sort/>
            <Advanced.Column
              property="systemMapping"
              header={this.i18n('acc:entity.RoleSystem.systemMapping')}
              cell={
                ({ rowIndex, data, property }) => {
                  const roleSystem = data[rowIndex];
                  const systemMapping = roleSystem[property];
                  return (
                    <Link to={`/system/${roleSystem.system.id}/mappings/${systemMapping.id}/detail`} >{systemMappingManager.getNiceLabel(systemMapping)}</Link>
                  );
                }
              }/>
          </Advanced.Table>
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
