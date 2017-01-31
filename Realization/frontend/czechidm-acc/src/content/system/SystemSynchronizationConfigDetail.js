import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Domain, Managers, Utils, Advanced } from 'czechidm-core';
import { SynchronizationConfigManager, SynchronizationLogManager, SystemMappingManager, SystemAttributeMappingManager} from '../../redux';
import ReconciliationMissingAccountActionTypeEnum from '../../domain/ReconciliationMissingAccountActionTypeEnum';
import SynchronizationLinkedActionTypeEnum from '../../domain/SynchronizationLinkedActionTypeEnum';
import SynchronizationMissingEntityActionTypeEnum from '../../domain/SynchronizationMissingEntityActionTypeEnum';
import SynchronizationUnlinkedActionTypeEnum from '../../domain/SynchronizationUnlinkedActionTypeEnum';
import IcFilterOperationTypeEnum from '../../domain/IcFilterOperationTypeEnum';

const uiKey = 'system-synchronization-config';
const uiKeyLogs = 'system-synchronization-logs';
const syncActionWfKey = 'eu.bcvsolutions.sync.action';
const synchronizationLogManager = new SynchronizationLogManager();
const synchronizationConfigManager = new SynchronizationConfigManager();
const systemMappingManager = new SystemMappingManager();
const systemAttributeMappingManager = new SystemAttributeMappingManager();
const workflowProcessDefinitionManager = new Managers.WorkflowProcessDefinitionManager();

class SystemSynchronizationConfigDetail extends Basic.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      ...this.state,
      systemMappingId: null, // dependant select box
      activeKey: 1,
      forceSearchCorrelationAttribute: new Domain.SearchParameters()
        .setFilter('mappingId', Domain.SearchParameters.BLANK_UUID) // dependant select box
    };
  }

  getManager() {
    return synchronizationLogManager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.system.systemSynchronizationConfigDetail';
  }

  showDetail(entity) {
    const {entityId} = this.props.params;
    this.context.router.push(`/system/${entityId}/synchronization-logs/${entity.id}/detail`);
  }

  componentWillReceiveProps(nextProps) {
    const { synchronizationConfigId} = nextProps.params;
    if (synchronizationConfigId && synchronizationConfigId !== this.props.params.synchronizationConfigId) {
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
    const {configId} = props.params;
    if (this._getIsNew(props)) {
      this.setState({synchronizationConfig: {
        system: props.location.query.systemId,
        linkedAction: SynchronizationLinkedActionTypeEnum.findKeyBySymbol(SynchronizationLinkedActionTypeEnum.UPDATE_ENTITY),
        unlinkedAction: SynchronizationUnlinkedActionTypeEnum.findKeyBySymbol(SynchronizationUnlinkedActionTypeEnum.LINK_AND_UPDATE_ACCOUNT),
        missingEntityAction: SynchronizationMissingEntityActionTypeEnum.findKeyBySymbol(SynchronizationMissingEntityActionTypeEnum.CREATE_ENTITY),
        missingAccountAction: ReconciliationMissingAccountActionTypeEnum.findKeyBySymbol(ReconciliationMissingAccountActionTypeEnum.IGNORE),
        filterOperation: IcFilterOperationTypeEnum.findKeyBySymbol(IcFilterOperationTypeEnum.GREATER_THAN)
      }});
    } else {
      this.context.store.dispatch(synchronizationConfigManager.fetchEntity(configId));
    }
    this.selectNavigationItems(['sys-systems', 'system-synchronization-configs']);
  }

  /**
   * Saves give entity
   */
  save(startSynchronization = false, event) {
    if (event) {
      event.preventDefault();
    }
    const formValid = this.refs.form.isFormValid();
    const formFilterValid = this.refs.formFilter.isFormValid();
    if (!formValid) {
      this.setState({activeKey: 1});
      return;
    }
    if (!formFilterValid) {
      this.setState({activeKey: 2});
      return;
    }
    this.setState({showLoading: true});
    const formEntity = this.refs.form.getData();
    // Merge filter data to form.
    const formFilter = this.refs.formFilter.getData();
    formEntity.customFilter = formFilter.customFilter;
    formEntity.filterAttribute = formFilter.filterAttribute;
    formEntity.filterOperation = formFilter.filterOperation;
    formEntity.tokenAttribute = formFilter.tokenAttribute;
    formEntity.customFilterScript = formFilter.customFilterScript;
    //
    formEntity.systemMapping = systemMappingManager.getSelfLink(formEntity.systemMapping);
    formEntity.correlationAttribute = systemAttributeMappingManager.getSelfLink(formEntity.correlationAttribute);
    formEntity.tokenAttribute = systemAttributeMappingManager.getSelfLink(formEntity.tokenAttribute);
    formEntity.filterAttribute = systemAttributeMappingManager.getSelfLink(formEntity.filterAttribute);
    if (formEntity.id === undefined) {
      this.context.store.dispatch(synchronizationConfigManager.createEntity(formEntity, `${uiKey}-detail`, (createdEntity, error) => {
        if (startSynchronization) {
          this.afterSaveAndStartSynchronization.bind(createdEntity, error);
        } else {
          this.afterSave(createdEntity, error);
        }
        if (!error && this.refs.table) {
          this.refs.table.getWrappedInstance().reload();
        }
      }));
    } else {
      this.context.store.dispatch(synchronizationConfigManager.patchEntity(formEntity, `${uiKey}-detail`,
         startSynchronization ? this.afterSaveAndStartSynchronization.bind(this) : this.afterSave.bind(this)));
    }
  }

  afterSave(entity, error) {
    if (!error) {
      if (this._getIsNew()) {
        this.addMessage({ message: this.i18n('create.success', { name: entity.name}) });
      } else {
        this.addMessage({ message: this.i18n('save.success', {name: entity.name}) });
      }
      const { entityId } = this.props.params;
      this.context.router.replace(`/system/${entityId}/synchronization-configs/`);
    } else {
      this.addError(error);
    }
    super.afterSave();
    this.setState({showLoading: false});
  }

  afterSaveAndStartSynchronization(entity, error) {
    if (!error) {
      if (this._getIsNew()) {
        this.addMessage({ message: this.i18n('create.success', { name: entity.name}) });
      } else {
        this.addMessage({ message: this.i18n('save.success', {name: entity.name}) });
      }
      this._startSynchronization(entity.id);
    } else {
      this.addError(error);
    }
    super.afterSave();
  }

  _getIsNew(nextProps) {
    const { query } = nextProps ? nextProps.location : this.props.location;
    return (query) ? query.new : null;
  }

  _startSynchronization(id) {
    this.refs[`confirm-delete`].show(
      this.i18n(`acc:content.system.systemSynchronizationConfigs.action.startSynchronization.message`),
      this.i18n(`acc:content.system.systemSynchronizationConfigs.action.startSynchronization.header`)
    ).then(() => {
      this.setState({
        showLoading: true
      });
      const promise = synchronizationConfigManager.getService().startSynchronization(id);
      promise.then((json) => {
        this.setState({
          showLoading: false
        });
        this.addMessage({ message: this.i18n('acc:content.system.systemSynchronizationConfigs.action.startSynchronization.success', { name: json.name }) });
        this.context.store.dispatch(synchronizationConfigManager.fetchEntity(id));
      }).catch(ex => {
        this.setState({
          showLoading: false
        });
        this.addError(ex);
        this.context.store.dispatch(synchronizationConfigManager.fetchEntity(id));
      });
    }, () => {
      // Rejected
    });
    return;
  }

  /**
   * Call after system mapping selection was changed. Create filter for correlationAttribute
   */
  _onChangeSystemMapping(systemMapping) {
    const systemMappingId = systemMapping ? systemMapping.id : null;
    this.setState({
      systemMappingId
    }, () => {
      // clear selected correlationAttribute
      this.refs.correlationAttribute.setValue(null);
      // clear selected tokenAttribute
      this.refs.tokenAttribute.setValue(null);
      // clear selected filterAttribute
      this.refs.filterAttribute.setValue(null);
    });
  }

  /**
   * Call after filter attribute selection was changed.
   */
  _onChangeFilterAttribute(filterAttribute) {
    if (!this.refs.tokenAttribute.getValue()) {
      this.refs.tokenAttribute.setValue(filterAttribute);
    }
  }

  _generateResultCell(rowIndex, data) {
    const actions = [];
    for (const action of data[rowIndex].syncActionLogs) {
      let level = 'default';
      if (action.operationResult === 'SUCCESS') {
        level = 'success';
      }
      if (action.operationResult === 'ERROR') {
        level = 'danger';
      }
      if (action.operationResult === 'WARNING') {
        level = 'warning';
      }
      if (action.operationResult === 'WF') {
        level = 'warning';
      }
      if (action.operationResult === 'IGNORE') {
        level = 'primary';
      }
      actions.push(
        <div>
            <Basic.Label style={{marginRight: '5px'}} level={level} text={action.operationCount}/>
            <label>{this.i18n(`acc:entity.SynchronizationLog.actions.${action.operationResult}.${action.syncAction}`)} </label>
        </div>);
    }
    return actions;
  }

  _onChangeSelectTabs(activeKey) {
    this.setState({activeKey});
  }

  render() {
    const { _showLoading, _synchronizationConfig} = this.props;
    const {systemMappingId, showLoading, activeKey} = this.state;
    const isNew = this._getIsNew();
    const innerShowLoading = isNew ? showLoading : (_showLoading || showLoading);
    const systemId = this.props.params.entityId;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('synchronizationConfigId', _synchronizationConfig ? _synchronizationConfig.id : Domain.SearchParameters.BLANK_UUID);
    const forceSearchMappingAttributes = new Domain.SearchParameters().setFilter('systemId', systemId || Domain.SearchParameters.BLANK_UUID);
    const forceSearchSyncActionWfKey = new Domain.SearchParameters().setFilter('category', syncActionWfKey);
    const synchronizationConfig = isNew ? this.state.synchronizationConfig : _synchronizationConfig;
    const attributeMappingIdFromEntity = synchronizationConfig && synchronizationConfig.systemMapping ? synchronizationConfig.systemMapping.id : null;
    const forceSearchCorrelationAttribute = new Domain.SearchParameters().setFilter('systemMappingId', systemMappingId || attributeMappingIdFromEntity || Domain.SearchParameters.BLANK_UUID);
    const tabLogShow = activeKey === 3;

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader>
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>

        <Basic.Tabs activeKey={activeKey} onSelect={this._onChangeSelectTabs.bind(this)}>
          <Basic.Tab eventKey={1} title={this.i18n('tabs.basicConfiguration.label')}>
            <form onSubmit={this.save.bind(this)}>
              <Basic.Panel className="no-border">
                <Basic.AbstractForm ref="form" data={synchronizationConfig} showLoading={innerShowLoading} className="form-horizontal">
                  <Basic.Checkbox
                    ref="enabled"
                    label={this.i18n('acc:entity.SynchronizationConfig.enabled')}/>
                  <Basic.Checkbox
                    ref="reconciliation"
                    label={this.i18n('acc:entity.SynchronizationConfig.reconciliation.label')}
                    helpBlock={this.i18n('acc:entity.SynchronizationConfig.reconciliation.help')}/>
                  <Basic.TextField
                    ref="name"
                    label={this.i18n('acc:entity.SynchronizationConfig.name')}
                    required/>
                  <Basic.SelectBox
                    ref="systemMapping"
                    manager={systemMappingManager}
                    forceSearchParameters={forceSearchMappingAttributes}
                    onChange={this._onChangeSystemMapping.bind(this)}
                    label={this.i18n('acc:entity.SynchronizationConfig.systemMapping')}
                    required/>
                  <Basic.TextField
                    ref="token"
                    label={this.i18n('acc:entity.SynchronizationConfig.token')}/>
                  <Basic.TextArea
                    ref="description"
                    label={this.i18n('acc:entity.SynchronizationConfig.description')}/>
                  <Basic.LabelWrapper label=" ">
                    <Basic.Alert
                       key="situationActionsAndWfInfo"
                       icon="exclamation-sign"
                       className="no-margin"
                       text={this.i18n('situationActionsAndWf')}/>
                  </Basic.LabelWrapper>
                  <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>{ this.i18n('acc:entity.SynchronizationConfig.linkedAction') }</h3>
                    <Basic.EnumSelectBox
                      className=""
                      ref="linkedAction"
                      enum={SynchronizationLinkedActionTypeEnum}
                      label={this.i18n('situationAction')}
                      required/>
                    <Basic.SelectBox
                      ref="linkedActionWfKey"
                      label={this.i18n('situationActionWf')}
                      forceSearchParameters={forceSearchSyncActionWfKey}
                      multiSelect={false}
                      manager={workflowProcessDefinitionManager}/>
                  <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>{ this.i18n('acc:entity.SynchronizationConfig.unlinkedAction') }</h3>
                    <Basic.EnumSelectBox
                      ref="unlinkedAction"
                      enum={SynchronizationUnlinkedActionTypeEnum}
                      label={this.i18n('situationAction')}
                      required/>
                    <Basic.SelectBox
                      ref="correlationAttribute"
                      manager={systemAttributeMappingManager}
                      forceSearchParameters={forceSearchCorrelationAttribute}
                      label={this.i18n('acc:entity.SynchronizationConfig.correlationAttribute')}
                      required/>
                    <Basic.SelectBox
                      ref="unlinkedActionWfKey"
                      label={this.i18n('situationActionWf')}
                      forceSearchParameters={forceSearchSyncActionWfKey}
                      multiSelect={false}
                      manager={workflowProcessDefinitionManager}/>
                  <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>{ this.i18n('acc:entity.SynchronizationConfig.missingEntityAction') }</h3>
                    <Basic.EnumSelectBox
                      ref="missingEntityAction"
                      enum={SynchronizationMissingEntityActionTypeEnum}
                      label={this.i18n('situationAction')}
                      required/>
                    <Basic.SelectBox
                      ref="missingEntityActionWfKey"
                      label={this.i18n('situationActionWf')}
                      forceSearchParameters={forceSearchSyncActionWfKey}
                      multiSelect={false}
                      manager={workflowProcessDefinitionManager}/>
                  <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>{ this.i18n('acc:entity.SynchronizationConfig.missingAccountAction') }</h3>
                    <Basic.EnumSelectBox
                      ref="missingAccountAction"
                      enum={ReconciliationMissingAccountActionTypeEnum}
                      label={this.i18n('situationAction')}
                      required/>
                    <Basic.SelectBox
                      ref="missingAccountActionWfKey"
                      label={this.i18n('situationActionWf')}
                      forceSearchParameters={forceSearchSyncActionWfKey}
                      multiSelect={false}
                      manager={workflowProcessDefinitionManager}/>
                </Basic.AbstractForm>
                <Basic.PanelFooter>
                  <Basic.Button type="button" level="link"
                    onClick={this.context.router.goBack}
                    showLoading={innerShowLoading}>
                    {this.i18n('button.back')}
                  </Basic.Button>
                  <Basic.Button
                    level="success"
                    onClick={this.save.bind(this, false)}
                    showLoading={innerShowLoading}
                    type="submit"
                    showLoadingIcon
                    showLoadingText={this.i18n('button.saving')}
                    rendered={Managers.SecurityManager.hasAuthority('SYSTEM_WRITE')}t>
                    {this.i18n('button.save')}
                  </Basic.Button>
                </Basic.PanelFooter>
              </Basic.Panel>
            </form>
          </Basic.Tab>
          <Basic.Tab eventKey={2} title={this.i18n('tabs.filterConfiguration.label')}>
            <form onSubmit={this.save.bind(this)}>
              <Basic.Panel className="no-border">
                <Basic.AbstractForm ref="formFilter" data={synchronizationConfig} showLoading={innerShowLoading} className="form-horizontal">
                  <Basic.Checkbox
                    ref="customFilter"
                    label={this.i18n('acc:entity.SynchronizationConfig.customFilter.label')}
                    helpBlock={this.i18n('acc:entity.SynchronizationConfig.customFilter.help')}/>
                  <Basic.SelectBox
                    ref="filterAttribute"
                    manager={systemAttributeMappingManager}
                    forceSearchParameters={forceSearchCorrelationAttribute}
                    label={this.i18n('acc:entity.SynchronizationConfig.filterAttribute.label')}
                    onChange={this._onChangeFilterAttribute.bind(this)}
                    tooltip={this.i18n('acc:entity.SynchronizationConfig.filterAttribute.help')}/>
                  <Basic.EnumSelectBox
                    ref="filterOperation"
                    enum={IcFilterOperationTypeEnum}
                    label={this.i18n('acc:entity.SynchronizationConfig.filterOperation')}
                    required/>
                  <Basic.SelectBox
                    ref="tokenAttribute"
                    manager={systemAttributeMappingManager}
                    forceSearchParameters={forceSearchCorrelationAttribute}
                    label={this.i18n('acc:entity.SynchronizationConfig.tokenAttribute.label')}
                    helpBlock={this.i18n('acc:entity.SynchronizationConfig.tokenAttribute.help')}/>
                  <Basic.ScriptArea
                    ref="customFilterScript"
                    height="20em"
                    helpBlock={this.i18n('acc:entity.SynchronizationConfig.customFilterScript.help')}
                    label={this.i18n('acc:entity.SynchronizationConfig.customFilterScript.label')}/>
                </Basic.AbstractForm>
                <Basic.PanelFooter>
                  <Basic.Button type="button" level="link"
                    onClick={this.context.router.goBack}
                    showLoading={innerShowLoading}>
                    {this.i18n('button.back')}
                  </Basic.Button>
                  <Basic.SplitButton
                    level="success"
                    title={this.i18n('button.saveAndContinue')}
                    onClick={this.save.bind(this, false)}
                    showLoading={innerShowLoading}
                    type="submit"
                    showLoadingIcon
                    showLoadingText={this.i18n('button.saving')}
                    rendered={Managers.SecurityManager.hasAuthority('SYSTEM_WRITE')}
                    pullRight
                    dropup>
                    <Basic.MenuItem
                      eventKey="1"
                      rendered={!isNew && Managers.SecurityManager.hasAuthority('SYNCHRONIZATION_WRITE')}
                      onClick={this.save.bind(this, true)}>
                      {this.i18n('button.saveAndStartSynchronization')}
                    </Basic.MenuItem>
                  </Basic.SplitButton>
                </Basic.PanelFooter>
              </Basic.Panel>
            </form>
          </Basic.Tab>
          <Basic.Tab eventKey={3} title={this.i18n('tabs.logs.label')}>
            <Basic.ContentHeader rendered={synchronizationConfig && !isNew} style={{ marginBottom: 0 }}>
              <Basic.Icon value="transfer"/>
              {' '}
              <span dangerouslySetInnerHTML={{ __html: this.i18n('synchronizationLogsHeader') }}/>
            </Basic.ContentHeader>
            <Basic.Panel rendered={tabLogShow && synchronizationConfig && !isNew} className="no-border">
              <Advanced.Table
                ref="table"
                uiKey={uiKeyLogs}
                manager={synchronizationLogManager}
                showLoading={innerShowLoading}
                forceSearchParameters={forceSearchParameters}
                showRowSelection={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_WRITE'])}
                rowClass={({rowIndex, data}) => { return data[rowIndex].containsError ? 'danger' : ''; }}
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
                      rendered={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_WRITE'])}>
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
                  <Advanced.Column property="running" face="boolean" header={this.i18n('acc:entity.SynchronizationLog.running')} sort/>
                  <Advanced.Column
                    property="syncActionLogs"
                    header={this.i18n('acc:entity.SynchronizationLog.results')}
                    cell={
                      ({ rowIndex, data }) => {
                        return this._generateResultCell(rowIndex, data);
                      }
                    }
                    />
                  <Advanced.Column property="started" face="datetime" header={this.i18n('acc:entity.SynchronizationLog.started')} sort/>
                  <Advanced.Column property="ended" face="datetime" header={this.i18n('acc:entity.SynchronizationLog.ended')} sort/>
                </Advanced.Table>
              </Basic.Panel>
            </Basic.Tab>
          </Basic.Tabs>
        </div>
    );
  }
}


SystemSynchronizationConfigDetail.propTypes = {
  _showLoading: PropTypes.bool,
};
SystemSynchronizationConfigDetail.defaultProps = {
  _showLoading: false,
};

function select(state, component) {
  const entity = Utils.Entity.getEntity(state, synchronizationConfigManager.getEntityType(), component.params.configId);
  if (entity) {
    entity.correlationAttribute = entity._embedded && entity._embedded.correlationAttribute ? entity._embedded.correlationAttribute : null;
    entity.tokenAttribute = entity._embedded && entity._embedded.tokenAttribute ? entity._embedded.tokenAttribute : null;
    entity.filterAttribute = entity._embedded && entity._embedded.filterAttribute ? entity._embedded.filterAttribute : null;
    entity.systemMapping = entity._embedded && entity._embedded.systemMapping ? entity._embedded.systemMapping : null;
  }
  return {
    _synchronizationConfig: entity,
    _showLoading: entity ? false : true,
  };
}

export default connect(select)(SystemSynchronizationConfigDetail);
