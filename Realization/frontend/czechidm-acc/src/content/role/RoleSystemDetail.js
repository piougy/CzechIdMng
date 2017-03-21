import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Domain, Managers, Utils, Advanced } from 'czechidm-core';
import { RoleSystemManager, SystemManager, RoleSystemAttributeManager, SystemMappingManager } from '../../redux';
import uuid from 'uuid';
import SystemOperationTypeEnum from '../../domain/SystemOperationTypeEnum';

const uiKey = 'role-system';
const uiKeyAttributes = 'role-system-attributes';
const roleSystemAttributeManager = new RoleSystemAttributeManager();
const systemManager = new SystemManager();
const roleSystemManager = new RoleSystemManager();
const roleManager = new Managers.RoleManager();
const systemMappingManager = new SystemMappingManager();

class RoleSystemDetail extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      ...this.state,
      systemId: null, // dependant select box
      systemMappingFilter: new Domain.SearchParameters()
        .setFilter('operationType', SystemOperationTypeEnum.findKeyBySymbol(SystemOperationTypeEnum.PROVISIONING))
        .setFilter('systemId', Domain.SearchParameters.BLANK_UUID) // dependant select box
    };
  }

  getManager() {
    return roleSystemAttributeManager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.role.roleSystemDetail';
  }

  showDetail(entity, add) {
    const role = entity._embedded && entity._embedded.role ? entity._embedded.role.id : this.props.params.entityId;
    const roleSystem = this.props.params.roleSystemId;
    if (add) {
      // When we add new object class, then we need id of role as parametr and use "new" url
      const uuidId = uuid.v1();
      this.context.router.push(`/role/${role}/systems/${roleSystem}/attributes/${uuidId}/new?new=1&mappingId=${entity.systemMapping}`);
    } else {
      this.context.router.push(`/role/${role}/systems/${roleSystem}/attributes/${entity.id}/detail`);
    }
  }

  componentWillReceiveProps(nextProps) {
    const { roleSystemId } = nextProps.params;
    if (roleSystemId && roleSystemId !== this.props.params.roleSystemId) {
      this._initComponent(nextProps);
    } else {
      // set persisted system for dependant select box
      this.setState({
        systemId: !this.props.roleSystem || this.props.roleSystem.system.id
      });
    }
  }

  // Did mount only call initComponent method
  componentDidMount() {
    this._initComponent(this.props);
  }

  /**
   * Method for init component from didMount method and from willReceiveProps method
   * @param  {properties of component} props For didmount call is this.props for call from willReceiveProps is nextProps.
   */
  _initComponent(props) {
    const { entityId, roleSystemId} = props.params;
    if (this._getIsNew(props)) {
      this.setState({
        roleSystem: {
          role: entityId
        },
        systemId: null
      });
    } else {
      this.context.store.dispatch(roleSystemManager.fetchEntity(roleSystemId));
    }
    this.selectNavigationItems(['roles', 'role-systems']);
  }

  /**
   * Saves give entity
   */
  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }

    const formEntity = this.refs.form.getData();
    formEntity.role = roleManager.getSelfLink(formEntity.role);
    formEntity.system = systemManager.getSelfLink(formEntity.system);
    formEntity.systemMapping = systemManager.getSelfLink(formEntity.systemMapping);
    if (formEntity.id === undefined) {
      this.context.store.dispatch(roleSystemManager.createEntity(formEntity, `${uiKey}-detail`, (createdEntity, error) => {
        this.afterSave(createdEntity, error);
        if (!error && this.refs.table) {
          this.refs.table.getWrappedInstance().reload();
        }
      }));
    } else {
      this.context.store.dispatch(roleSystemManager.patchEntity(formEntity, `${uiKey}-detail`, this.afterSave.bind(this)));
    }
  }

  afterSave(entity, error) {
    if (!error) {
      if (this._getIsNew()) {
        this.addMessage({ message: this.i18n('create.success', { system: entity._embedded.system.name, role: entity._embedded.role.name }) });
        this.context.router.replace(`/role/${entity._embedded.role.id}/systems/${entity.id}/detail`, {entityId: entity.id});
      } else {
        this.addMessage({ message: this.i18n('save.success', { system: entity._embedded.system.name, role: entity._embedded.role.name }) });
      }
    } else {
      this.addError(error);
    }
    super.afterSave();
  }

  closeDetail() {
    this.refs.form.processEnded();
  }

  _getIsNew(nextProps) {
    const { query } = nextProps ? nextProps.location : this.props.location;
    return (query) ? query.new : null;
  }

  /**
   * Set filter to dependant select box
   *
   * @param  {System} system selected system
   */
  onChangeSystem(system) {
    const systemId = system ? system.id : null;
    this.setState({
      systemId,
      systemMappingFilter: this.state.systemMappingFilter.setFilter('systemId', systemId || Domain.SearchParameters.BLANK_UUID)
    }, () => {
      // clear selected systemMapping
      this.refs.systemMapping.setValue(null);
    });
  }

  render() {
    const { _showLoading, _roleSystem } = this.props;
    const { systemMappingFilter, systemId } = this.state;
    //
    const forceSearchParameters = new Domain.SearchParameters().setFilter('roleSystemId', _roleSystem ? _roleSystem.id : Domain.SearchParameters.BLANK_UUID);
    const isNew = this._getIsNew();
    const roleSystem = isNew ? this.state.roleSystem : _roleSystem;
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader>
          <Basic.Icon value="link"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>

        <form onSubmit={this.save.bind(this)}>
          <Basic.Panel className="no-border">
            <Basic.AbstractForm ref="form" data={roleSystem} showLoading={_showLoading} style={{ padding: 0 }}>
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
                readOnly={!isNew}
                required
                onChange={this.onChangeSystem.bind(this)}/>
              <Basic.SelectBox
                ref="systemMapping"
                manager={systemMappingManager}
                forceSearchParameters={systemMappingFilter}
                label={this.i18n('acc:entity.RoleSystem.systemMapping')}
                placeholder={systemId ? null : this.i18n('systemMapping.systemPlaceholder')}
                readOnly={!isNew || !systemId}
                required/>
            </Basic.AbstractForm>
            <Basic.PanelFooter rendered={Utils.Entity.isNew(roleSystem)}>
              <Basic.Button type="button" level="link"
                onClick={this.context.router.goBack}
                showLoading={_showLoading}>
                {this.i18n('button.back')}
              </Basic.Button>
              <Basic.Button
                onClick={this.save.bind(this)}
                level="success"
                type="submit"
                showLoading={_showLoading}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
        </form>
        <Basic.ContentHeader rendered={roleSystem && !isNew} style={{ marginBottom: 0 }}>
          <Basic.Icon value="list-alt"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('roleSystemAttributesHeader') }}/>
        </Basic.ContentHeader>
        <Basic.Panel rendered={roleSystem && !isNew} className="no-border last">
          <Advanced.Table
            ref="table"
            uiKey={uiKeyAttributes}
            manager={roleSystemAttributeManager}
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
                <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  onClick={this.showDetail.bind(this, roleSystem, true)}
                  rendered={Managers.SecurityManager.hasAnyAuthority(['ROLE_WRITE'])}>
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
                to={`role/${roleSystem ? roleSystem.role.id : null }/systems/${roleSystem ? roleSystem.id : null}/attributes/:id/detail`}
                property="name"
                header={this.i18n('acc:entity.RoleSystemAttribute.name.label')}
                sort />
              <Advanced.Column property="idmPropertyName" header={this.i18n('acc:entity.SystemAttributeMapping.idmPropertyName.label')} sort/>
              <Advanced.Column property="uid" face="boolean" header={this.i18n('acc:entity.SystemAttributeMapping.uid.label')} sort/>
              <Advanced.Column property="entityAttribute" face="boolean" header={this.i18n('acc:entity.SystemAttributeMapping.entityAttribute')} sort/>
              <Advanced.Column property="extendedAttribute" face="boolean" header={this.i18n('acc:entity.SystemAttributeMapping.extendedAttribute')} sort/>
              <Advanced.Column property="disabledDefaultAttribute" face="boolean" header={this.i18n('acc:entity.RoleSystemAttribute.disabledDefaultAttribute')} sort/>
              <Advanced.Column property="transformScript" face="boolean" header={this.i18n('acc:entity.RoleSystemAttribute.transformScriptTable')}/>
            </Advanced.Table>
          </Basic.Panel>
        </div>
    );
  }
}

RoleSystemDetail.propTypes = {
  _showLoading: PropTypes.bool,
};
RoleSystemDetail.defaultProps = {
  _showLoading: false,
};

function select(state, component) {
  const entity = Utils.Entity.getEntity(state, roleSystemManager.getEntityType(), component.params.roleSystemId);
  if (entity) {
    entity.role = entity._embedded && entity._embedded.role ? entity._embedded.role : null;
    entity.system = entity._embedded && entity._embedded.system ? entity._embedded.system.id : null;
    entity.systemMapping = entity._embedded && entity._embedded.systemMapping ? entity._embedded.systemMapping.id : null;
  }
  return {
    _roleSystem: entity,
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(RoleSystemDetail);
