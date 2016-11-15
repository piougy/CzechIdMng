import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Domain, Managers, Utils, Advanced } from 'czechidm-core';
import { SystemEntityHandlingManager, SystemManager, SchemaAttributeHandlingManager } from '../../redux';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';
import SystemOperationTypeEnum from '../../domain/SystemOperationTypeEnum';
import uuid from 'uuid';

const uiKey = 'system-entities-handling';
const uiKeyAttributes = 'schema-attributes-handling';
const schemaAttributeHandlingManager = new SchemaAttributeHandlingManager();
const systemManager = new SystemManager();
const systemEntityHandlingManager = new SystemEntityHandlingManager();

class SystemEntityHandlingDetail extends Basic.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getManager() {
    return schemaAttributeHandlingManager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.system.entityHandlingDetail';
  }

  showDetail(entity, add) {
    if (add) {
      const uuidId = uuid.v1();
      const entityHandlingId = this.props._entityHandling.id;
      const systemId = this.props._entityHandling.system;
      this.context.router.push(`/schema-attributes-handling/${uuidId}/new?new=1&entityHandlingId=${entityHandlingId}&systemId=${systemId}`);
    } else {
      this.context.router.push(`/schema-attributes-handling/${entity.id}/detail`);
    }
  }

  componentWillReceiveProps(nextProps) {
    const { entityId} = nextProps.params;
    if (entityId && entityId !== this.props.params.entityId) {
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
    const { entityId} = props.params;
    if (this._getIsNew(props)) {
      this.setState({entityHandling: {
        system: props.location.query.systemId,
        entityType: SystemEntityTypeEnum.findKeyBySymbol(SystemEntityTypeEnum.IDENTITY),
        operationType: SystemOperationTypeEnum.findKeyBySymbol(SystemOperationTypeEnum.PROVISIONING)
      }});
    } else {
      this.context.store.dispatch(systemEntityHandlingManager.fetchEntity(entityId));
    }
    this.selectNavigationItems(['sys-systems']);
  }

  /**
   * Saves give entity
   */
  save(entity, event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }

    const formEntity = this.refs.form.getData();
    formEntity.system = systemManager.getSelfLink(formEntity.system);
    if (formEntity.id === undefined) {
      this.context.store.dispatch(systemEntityHandlingManager.createEntity(formEntity, `${uiKey}-detail`, (createdEntity, error) => {
        this.afterSave(createdEntity, error);
        if (!error) {
          this.refs.table.getWrappedInstance().reload();
        }
      }));
    } else {
      this.context.store.dispatch(systemEntityHandlingManager.patchEntity(formEntity, `${uiKey}-detail`, this.afterSave.bind(this)));
    }
  }

  afterSave(entity, error) {
    if (!error) {
      if (this._getIsNew()) {
        this.addMessage({ message: this.i18n('create.success', { entityType: entity.entityType, operationType: entity.operationType}) });
        this.context.router.replace(`/system-entities-handling/${entity.id}/detail`, {entityId: entity.id});
      } else {
        this.addMessage({ message: this.i18n('save.success', {entityType: entity.entityType, operationType: entity.operationType}) });
      }
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

  render() {
    const { _showLoading, _entityHandling} = this.props;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('entityHandlingId', _entityHandling ? _entityHandling.id : Domain.SearchParameters.BLANK_UUID);
    const isNew = this._getIsNew();
    const entityHandling = isNew ? this.state.entityHandling : _entityHandling;
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader>
          <span dangerouslySetInnerHTML={{ __html: this.i18n('systemEntityHandlingHeader') }}/>
        </Basic.ContentHeader>

        <form onSubmit={this.save.bind(this)}>
          <Basic.Panel>
            <Basic.AbstractForm ref="form" data={entityHandling} showLoading={_showLoading} className="form-horizontal">
              <Basic.SelectBox
                ref="system"
                manager={systemManager}
                label={this.i18n('acc:entity.SystemEntityHandling.system')}
                readOnly
                required/>
              <Basic.EnumSelectBox
                ref="entityType"
                enum={SystemEntityTypeEnum}
                label={this.i18n('acc:entity.SystemEntityHandling.entityType')}
                required/>
              <Basic.EnumSelectBox
                ref="operationType"
                enum={SystemOperationTypeEnum}
                label={this.i18n('acc:entity.SystemEntityHandling.operationType')}
                required/>
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
        <Basic.ContentHeader rendered={entityHandling && !isNew}>
          <Basic.Icon value="list-alt"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('schemaAttributesHandlingHeader') }}/>
        </Basic.ContentHeader>
        <Basic.Panel rendered={entityHandling && !isNew}>
          <Advanced.Table
            ref="table"
            uiKey={uiKeyAttributes}
            manager={schemaAttributeHandlingManager}
            forceSearchParameters={forceSearchParameters}
            showRowSelection={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_WRITE'])}
            actions={
              Managers.SecurityManager.hasAnyAuthority(['SYSTEM_WRITE'])
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
                  rendered={Managers.SecurityManager.hasAnyAuthority(['ROLE_WRITE'])}>
                  <Basic.Icon type="fa" icon="plus"/>
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              ]
            }
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm" className="form-horizontal">
                  <Basic.Row className="last">
                    <div className="col-lg-6">
                      <Advanced.Filter.TextField
                        ref="idmPropertyName"
                        label={this.i18n('filter.idmPropertyName.label')}
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
                to="schema-attributes-handling/:id/detail"
                property="idmPropertyName"
                header={this.i18n('acc:entity.SchemaAttributeHandling.idmPropertyName')}
                sort />
              <Advanced.Column property="schemaAttribute.name" header={this.i18n('acc:entity.SchemaAttributeHandling.schemaAttribute')} sort/>
              <Advanced.Column property="extendedAttribute" face="boolean" header={this.i18n('acc:entity.SchemaAttributeHandling.extendedAttribute')} sort/>
            </Advanced.Table>
          </Basic.Panel>
        </div>
    );
  }
}

SystemEntityHandlingDetail.propTypes = {
  _showLoading: PropTypes.bool,
};
SystemEntityHandlingDetail.defaultProps = {
  _showLoading: false,
};

function select(state, component) {
  const entity = Utils.Entity.getEntity(state, systemEntityHandlingManager.getEntityType(), component.params.entityId);
  if (entity) {
    const system = entity._embedded && entity._embedded.system ? entity._embedded.system.id : null;
    entity.system = system;
  }
  return {
    _entityHandling: entity,
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(SystemEntityHandlingDetail);
