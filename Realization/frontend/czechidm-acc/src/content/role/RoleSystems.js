import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import { Basic, Advanced, Domain, Managers, Utils } from 'czechidm-core';
import { RoleSystemManager, SystemManager } from '../../redux';

const uiKey = 'role-systems-table';
const manager = new RoleSystemManager();
const systemManager = new SystemManager();
const roleManager = new Managers.RoleManager();

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

  showDetail(entity) {
    const entityFormData = _.merge({}, entity, {
      role: entity.id && entity._embedded.role ? entity._embedded.role.id : this.props.params.entityId,
      system: entity.id && entity._embedded.system ? entity._embedded.system.id : null
    });

    super.showDetail(entityFormData, () => {
      this.refs.system.focus();
    });
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    formEntity.role = roleManager.getSelfLink(formEntity.role);
    formEntity.system = systemManager.getSelfLink(formEntity.system);
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ message: this.i18n('save.success', { system: entity._embedded.system.name, role: entity._embedded.role.name }) });
    }
    super.afterSave(entity, error);
  }

  render() {
    const { entityId } = this.props.params;
    const { _showLoading, role } = this.props;
    const { detail } = this.state;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('roleId', entityId);

    const _role = role || {};

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
                <Basic.Button level="success" key="add_button" className="btn-xs" onClick={this.showDetail.bind(this, {})} rendered={Managers.SecurityManager.hasAnyAuthority(['ROLE_WRITE'])}>
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
                      onClick={this.showDetail.bind(this, data[rowIndex])}/>
                  );
                }
              }/>
            <Advanced.ColumnLink
              to="/system/:_target/detail"
              target="_embedded.system.id"
              access={{ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ']}}
              property="_embedded.system.name"
              header={this.i18n('acc:entity.RoleSystem.system')}
              sort/>
            <Advanced.Column property="type" header={this.i18n('acc:entity.RoleSystem.type')} sort face="text" />
          </Advanced.Table>
        </Basic.Panel>

        <Basic.Modal
          bsSize="default"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this, {})}>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header', { role: _role.name })} rendered={detail.entity.id === undefined}/>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { name: detail.entity.name, role: _role.name })} rendered={detail.entity.id !== undefined}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm ref="form" showLoading={_showLoading} className="form-horizontal">
                <Basic.SelectBox
                  ref="role"
                  manager={roleManager}
                  label={this.i18n('acc:entity.RoleSystem.role')}
                  readOnly
                  required/>
                <Basic.SelectBox
                  ref="system"
                  manager={systemManager}
                  label={this.i18n('acc:entity.RoleSystem.system')}
                  required/>
                <Basic.TextField
                  ref="type"
                  label={this.i18n('acc:entity.RoleSystem.type')}
                  required
                  max={255}/>
              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={this.closeDetail.bind(this)}
                showLoading={_showLoading}>
                {this.i18n('button.close')}
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={_showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
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
