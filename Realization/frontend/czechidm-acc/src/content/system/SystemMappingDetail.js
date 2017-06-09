import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import Joi from 'joi';
//
import { Basic, Domain, Managers, Utils, Advanced } from 'czechidm-core';
import { SystemMappingManager, SystemManager, SystemAttributeMappingManager, SchemaObjectClassManager } from '../../redux';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';
import SystemOperationTypeEnum from '../../domain/SystemOperationTypeEnum';
import uuid from 'uuid';

const uiKey = 'system-mappings';
const uiKeyAttributes = 'system-attribute-mappings';
const systemAttributeMappingManager = new SystemAttributeMappingManager();
const systemManager = new SystemManager();
const treeTypeManager = new Managers.TreeTypeManager();
const systemMappingManager = new SystemMappingManager();
const schemaObjectClassManager = new SchemaObjectClassManager();

class SystemMappingDetail extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getUiKey() {
    return uiKey;
  }

  getManager() {
    // returns manager for underlying table
    return systemAttributeMappingManager;
  }

  getContentKey() {
    return 'acc:content.system.mappingDetail';
  }

  showDetail(entity, add) {
    const mappingId = this.props._mapping.id;
    const systemId = this.props._mapping.system.id;
    const objectClassId = this.props._mapping.objectClass.id;
    if (add) {
      const uuidId = uuid.v1();
      this.context.router.push(`/system/${systemId}/attribute-mappings/${uuidId}/new?new=1&mappingId=${mappingId}&objectClassId=${objectClassId}`);
    } else {
      this.context.router.push(`/system/${systemId}/attribute-mappings/${entity.id}/detail`);
    }
  }

  componentWillReceiveProps(nextProps) {
    const { mappingId } = nextProps.params;
    if (mappingId && mappingId !== this.props.params.mappingId) {
      this._initComponent(nextProps);
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
    const { entityId, mappingId } = props.params;
    if (this._getIsNew(props)) {
      this.setState({
        mapping: {
          system: entityId,
          entityType: SystemEntityTypeEnum.findKeyBySymbol(SystemEntityTypeEnum.IDENTITY),
          operationType: SystemOperationTypeEnum.findKeyBySymbol(SystemOperationTypeEnum.PROVISIONING)
        }
      });
    } else {
      this.context.store.dispatch(systemMappingManager.fetchEntity(mappingId));
    }
    this.selectNavigationItems(['sys-systems', 'system-mappings']);
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
    formEntity.system = systemManager.getSelfLink(formEntity.system);
    formEntity.objectClass = schemaObjectClassManager.getSelfLink(formEntity.objectClass);
    formEntity.treeType = treeTypeManager.getSelfLink(formEntity.treeType);
    if (formEntity.id === undefined) {
      this.context.store.dispatch(systemMappingManager.createEntity(formEntity, `${uiKey}-detail`, (createdEntity, error) => {
        this.afterSave(createdEntity, error);
        if (!error && this.refs.table) {
          this.refs.table.getWrappedInstance().reload();
        }
      }));
    } else {
      this.context.store.dispatch(systemMappingManager.patchEntity(formEntity, `${uiKey}-detail`, this.afterSave.bind(this)));
    }
  }

  afterSave(entity, error) {
    if (!error) {
      if (this._getIsNew()) {
        this.addMessage({ message: this.i18n('create.success', { entityType: entity.entityType, operationType: entity.operationType}) });
      } else {
        this.addMessage({ message: this.i18n('save.success', {entityType: entity.entityType, operationType: entity.operationType}) });
      }
      const { entityId } = this.props.params;
      this.context.router.replace(`/system/${entityId}/mappings/${entity.id}/detail`, { mappingId: entity.id });
    } else {
      this.addError(error);
    }
    super.afterSave();
  }

  _getIsNew(nextProps) {
    const { query } = nextProps ? nextProps.location : this.props.location;
    return (query) ? query.new : null;
  }

  _onChangeEntityType(entity) {
    this.setState({_entityType: entity.value});
  }

  render() {
    const { _showLoading, _mapping } = this.props;
    const { _entityType} = this.state;
    const isNew = this._getIsNew();
    const mapping = isNew ? this.state.mapping : _mapping;

    let isSelectedTree = false;
    if (_entityType !== undefined) {
      if (_entityType === SystemEntityTypeEnum.findKeyBySymbol(SystemEntityTypeEnum.TREE)) {
        isSelectedTree = true;
      }
    } else {
      if (mapping && mapping.entityType === SystemEntityTypeEnum.findKeyBySymbol(SystemEntityTypeEnum.TREE)) {
        isSelectedTree = true;
      }
    }

    let isSelectedIdentity = false;
    if (_entityType !== undefined) {
      if (_entityType === SystemEntityTypeEnum.findKeyBySymbol(SystemEntityTypeEnum.IDENTITY)) {
        isSelectedIdentity = true;
      }
    } else {
      if (mapping && mapping.entityType === SystemEntityTypeEnum.findKeyBySymbol(SystemEntityTypeEnum.IDENTITY)) {
        isSelectedIdentity = true;
      }
    }

    let isSelectedProvisioning = false;
    if (mapping && mapping.operationType === 'PROVISIONING') {
      isSelectedProvisioning = true;
    }

    const systemId = this.props.params.entityId;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemMappingId', _mapping ? _mapping.id : Domain.SearchParameters.BLANK_UUID);
    const objectClassSearchParameters = new Domain.SearchParameters().setFilter('systemId', systemId ? systemId : Domain.SearchParameters.BLANK_UUID);

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader>
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>

        <form onSubmit={this.save.bind(this)}>
          <Basic.Panel className="no-border">
            <Basic.AbstractForm ref="form" data={mapping} showLoading={_showLoading}>
              <Basic.SelectBox
                ref="system"
                manager={systemManager}
                hidden
                label={this.i18n('acc:entity.SystemMapping.system')}
                readOnly/>
              <Basic.EnumSelectBox
                ref="operationType"
                enum={SystemOperationTypeEnum}
                label={this.i18n('acc:entity.SystemMapping.operationType')}
                required/>
              <Basic.TextField
                ref="name"
                label={this.i18n('acc:entity.SystemMapping.name')}
                required/>
              <Basic.SelectBox
                ref="objectClass"
                manager={schemaObjectClassManager}
                forceSearchParameters={objectClassSearchParameters}
                label={this.i18n('acc:entity.SystemMapping.objectClass')}
                readOnly={!Utils.Entity.isNew(mapping)}
                required/>
              <Basic.EnumSelectBox
                ref="entityType"
                enum={SystemEntityTypeEnum}
                onChange={this._onChangeEntityType.bind(this)}
                label={this.i18n('acc:entity.SystemMapping.entityType')}
                readOnly={!Utils.Entity.isNew(mapping)}
                required/>
              <Basic.SelectBox
                ref="treeType"
                label={this.i18n('acc:entity.SystemMapping.treeType')}
                hidden={!isSelectedTree }
                required={isSelectedTree}
                manager={treeTypeManager}
              />
              <Basic.Checkbox
                ref="protectionEnabled"
                label={this.i18n('acc:entity.SystemMapping.protectionEnabled')}
                helpBlock={this.i18n('protectionEnabled.help')}
                hidden={!isSelectedIdentity || !isSelectedProvisioning || isNew}
              />
              <Basic.TextField
                style={{maxWidth: '300px'}}
                ref="protectionInterval"
                validation={Joi.number().allow(null).integer().min(1).max(2147483647)}
                label={this.i18n('acc:entity.SystemMapping.protectionInterval')}
                hidden={!isSelectedIdentity || !isSelectedProvisioning || isNew}
              />
            </Basic.AbstractForm>
            <Basic.PanelFooter>
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
        <Basic.ContentHeader rendered={mapping && !isNew} style={{ marginBottom: 0 }}>
          <Basic.Icon value="list-alt"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('systemAttributesMappingHeader') }}/>
        </Basic.ContentHeader>
        <Basic.Panel rendered={mapping && !isNew} className="no-border">
          <Advanced.Table
            ref="table"
            uiKey={uiKeyAttributes}
            manager={systemAttributeMappingManager}
            forceSearchParameters={forceSearchParameters}
            showRowSelection={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])}
            rowClass={({rowIndex, data}) => { return data[rowIndex].disabledAttribute ? 'disabled' : ''; }}
            actions={
              Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])
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
                  onClick={this.showDetail.bind(this, { }, true)}
                  rendered={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])}>
                  <Basic.Icon type="fa" icon="plus"/>
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              ]
            }
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row className="last">
                    <div className="col-lg-6">
                      <Advanced.Filter.TextField
                        ref="idmPropertyName"
                        placeholder={this.i18n('filter.idmPropertyName.placeholder')}/>
                    </div>
                    <div className="col-lg-2"/>
                    <div className="col-lg-4 text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                    </div>
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
              <Advanced.ColumnLink
                to={`system/${systemId}/attribute-mappings/:id/detail`}
                property="name"
                header={this.i18n('acc:entity.SystemAttributeMapping.name.label')}
                sort />
              <Advanced.Column property="idmPropertyName" header={this.i18n('acc:entity.SystemAttributeMapping.idmPropertyName.label')} sort/>
              <Advanced.Column property="uid" face="boolean" header={this.i18n('acc:entity.SystemAttributeMapping.uid.label')} sort/>
              <Advanced.Column property="entityAttribute" face="boolean" header={this.i18n('acc:entity.SystemAttributeMapping.entityAttribute')} sort/>
              <Advanced.Column property="extendedAttribute" face="boolean" header={this.i18n('acc:entity.SystemAttributeMapping.extendedAttribute.label')} sort/>
              <Advanced.Column property="transformationFromResource" face="boolean" header={this.i18n('acc:entity.SystemAttributeMapping.transformationFromResource')}/>
              <Advanced.Column property="transformationToResource" face="boolean" header={this.i18n('acc:entity.SystemAttributeMapping.transformationToResource')}/>
            </Advanced.Table>
          </Basic.Panel>
        </div>
    );
  }
}

SystemMappingDetail.propTypes = {
  _showLoading: PropTypes.bool,
};
SystemMappingDetail.defaultProps = {
  _showLoading: false,
};

function select(state, component) {
  const entity = Utils.Entity.getEntity(state, systemMappingManager.getEntityType(), component.params.mappingId);
  if (entity && entity._embedded && entity._embedded.objectClass) {
    entity.system = entity._embedded.objectClass.system;
    entity.objectClass = entity._embedded.objectClass;
    entity.treeType = entity._embedded.treeType;
  }
  return {
    _mapping: entity,
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(SystemMappingDetail);
