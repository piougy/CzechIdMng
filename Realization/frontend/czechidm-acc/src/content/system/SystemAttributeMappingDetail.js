import PropTypes from 'prop-types';
import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import Joi from 'joi';
//
import { Advanced, Basic, Domain, Enums, Managers, Utils } from 'czechidm-core';
import MappingContextCompleters from 'czechidm-core/src/content/script/completers/MappingContextCompleters';
import { AttributeControlledValueManager, SchemaAttributeManager, SystemAttributeMappingManager, SystemMappingManager } from '../../redux';
import AttributeMappingStrategyTypeEnum from '../../domain/AttributeMappingStrategyTypeEnum';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';
import AttributeControlledValueTable from './AttributeControlledValueTable';
import { RoleSystemAttributeTable } from '../role/RoleSystemAttributeTable';

const uiKey = 'system-attribute-mapping';
const manager = new SystemAttributeMappingManager();
const controlledValueManager = new AttributeControlledValueManager();
const systemMappingManager = new SystemMappingManager();
const schemaAttributeManager = new SchemaAttributeManager();
const scriptManager = new Managers.ScriptManager();
// password attributes is from 9.3.0 marked as passwordAttribute (boolean),
// constant is used for automatic check passwordAttribute
const PASSWORD_ATTRIBUTE = '__PASSWORD__';

class SystemAttributeMappingDetail extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      activeKey: 1,
      strategyType: null
    };
    this.scriptManager = new Managers.ScriptManager();
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.system.attributeMappingDetail';
  }

  // eslint-disable-next-line camelcase
  UNSAFE_componentWillReceiveProps(nextProps) {
    const {_attribute} = nextProps;

    if (_attribute && _attribute !== this.props._attribute) {
      if (_attribute) {
        this.setState({
          disabledAttribute: _attribute.disabledAttribute,
          entityAttribute: _attribute.entityAttribute,
          extendedAttribute: _attribute.extendedAttribute,
          passwordAttribute: _attribute.passwordAttribute,
          sendOnPasswordChange: _attribute.sendOnPasswordChange
        }, () => {
          if (this.refs.form) {
            // Workaround - We need set value after enumeration type was changed
            this.refs.idmPropertyEnum.setValue(_attribute.idmPropertyEnum);
          }
        });
      }
    }
  }

  // Did mount only call initComponent method
  componentDidMount() {
    this._initComponent(this.props);
  }

  /**
   * Method for init component from didMount method and from willReceiveProps method
   * @param  props - properties of component - props For didmount call is this.props for call from willReceiveProps is nextProps.
   */
  _initComponent(props) {
    const { attributeId} = props.match.params;
    if (this._getIsNew(props)) {
      this.setState({
        attribute: {
          systemMapping: props.location.query.mappingId,
          objectClassId: props.location.query.objectClassId,
          cached: true,
          strategyType: AttributeMappingStrategyTypeEnum.findKeyBySymbol(AttributeMappingStrategyTypeEnum.SET),
          echoTimeout: 180 // Standard timeout for echo - 3 min
        }
      });
    //  this.context.store.dispatch(systemMappingManager.fetchEntity(props.location.query.mappingId));
    } else {
      this.context.store.dispatch(this.getManager().fetchEntity(attributeId));
    }
    this.selectNavigationItems(['sys-systems', 'system-mappings']);
  }

  _getIsNew(nextProps) {
    if ((nextProps && nextProps.location) || this.props.location) {
      const { query } = nextProps ? nextProps.location : this.props.location;
      return (query) ? query.new : null;
    }
    return false;
  }

  save(event) {
    const formEntity = this.refs.form.getData();
    // Password filter is active only for password attributes
    if (this.refs.formPasswordFilter) {
      const formPasswordFilter = this.refs.formPasswordFilter.getData();
      // Update attribute by password filter form
      formEntity.passwordFilter = formPasswordFilter.passwordFilter;
      formEntity.echoTimeout = formPasswordFilter.echoTimeout;
      formEntity.transformationUidScript = formPasswordFilter.transformationUidScript;
    }
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      if (this._getIsNew()) {
        this.addMessage({ message: this.i18n('create.success', { name: entity.name }) });
      } else {
        this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
      }
      // Go to parent wizard component.
      if (this.isWizard()) {
        this.goBack();
      } else {
        this.context.history.goBack();
      }
    } else {
      this.addError(error);
    }
    super.afterSave();
  }

  goBack() {
    if (this.isWizard()) {
      // If is component in the wizard, then set new ID (master component)
      // to the active action and render wizard.
      const activeStep = this.context.wizardContext.activeStep;
      if (activeStep) {
        activeStep.id = 'mappingAttributes';
        activeStep.mapping = this.props._systemMapping;
        this.context.wizardContext.wizardForceUpdate();
      }
    } else {
      this.context.history.goBack();
    }
  }

  wizardAddButtons(showLoading) {
    return this.renderButtons(showLoading);
  }

  closeDetail() {
    if (this.refs.form) {
      this.refs.form.processEnded();
    }
  }

  _checkboxChanged(key, uncheckKey, event) {
    const checked = event.currentTarget.checked;
    this.setState({[key]: checked}, () => {
      if (checked && uncheckKey !== null) {
        this.setState({[uncheckKey]: false});
        this.refs[uncheckKey].setState({value: false}, () => {
          this.forceUpdate();
        });
      }
      if (key === 'entityAttribute') {
        this.refs.idmPropertyName.setValue(null);
        this.refs.idmPropertyEnum.setValue(null);
      }
      if (key === 'passwordAttribute') {
        this.refs.entityAttribute.setValue(false);
        this.refs.extendedAttribute.setValue(false);
        this.refs.idmPropertyName.setValue(null);
        this.refs.idmPropertyEnum.setValue(null);
      }
    });
  }

  _schemaAttributeChange(value) {
    let passwordSelected = false;
    if (value && value.name === PASSWORD_ATTRIBUTE) {
      passwordSelected = true;
    }
    this.refs.passwordAttribute.setValue(passwordSelected);
    // Set info alert for password attribute
    this.setState({
      passwordAttribute: passwordSelected
    });

    if (!this.refs.name.getValue()) {
      this.refs.name.setValue(value.name);
    }
  }

  _strategyTypeChange(value) {
    if (!value) {
      return;
    }
    this.setState({
      strategyType: value.value
    });
  }

  _onChangeEntityEnum(item) {
    const {_systemMapping} = this.props;
    if (item) {
      const field = SystemEntityTypeEnum.getEntityEnum(_systemMapping ? _systemMapping.entityType : 'IDENTITY').getField(item.value);
      this.refs.idmPropertyName.setValue(field);
      this.setState({_idmPropertyName: field});
    } else {
      this.refs.idmPropertyName.setValue(null);
      this.setState({_idmPropertyName: null});
    }
  }

  _onChangeSelectTabs(activeKey) {
    this.setState({activeKey});
  }

  _getCompletersToResource() {
    return [
      {
        name: 'uid',
        returnType: 'String',
        description: 'Identifier of the account for which provisioning is performed (AccAccount.uid).'
      },
      {
        name: 'attributeValue',
        returnType: 'Object',
        description: 'The value of the selected entity/EAV attribute.'
      },
      {
        name: 'system',
        returnType: 'SysSystemDto',
        description: 'DTO for this system.'
      },
      {
        name: 'entity',
        returnType: 'AbstractDto',
        description: 'Entity for which provisioning is performed. Its specific type depends on the type of mapping (IdmIdentityDto / IdmRoleDto ...).'
      }, ...MappingContextCompleters.getCompleters()
    ];
  }

  _getCompletersFromResource() {
    return [
      {
        name: 'attributeValue',
        returnType: 'Object',
        description: 'The value of the selected connector object attribute.'
      },
      {
        name: 'system',
        returnType: 'SysSystemDto',
        description: 'DTO for this system.'
      },
      {
        name: 'icAttributes',
        returnType: 'List<IcAttribute>',
        description: 'An attributes of object from the end system.'
      }
    ];
  }

  renderButtons(_showLoading) {
    return (
      <span>
        <Basic.Button
          type="button"
          level="link"
          onClick={this.goBack.bind(this)}
          showLoading={_showLoading}>
          {this.i18n('button.back')}
        </Basic.Button>
        <Basic.Button
          level="success"
          type={this.isWizard() ? 'button' : 'submit'}
          onClick={this.isWizard() ? this.save.bind(this) : null}
          showLoading={_showLoading}
          rendered={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])}>
          {this.i18n('button.save')}
        </Basic.Button>
      </span>
    );
  }

  render() {
    const { _showLoading, _attribute, _systemMapping } = this.props;
    const { disabledAttribute,
      entityAttribute,
      extendedAttribute,
      activeKey,
      strategyType,
      passwordAttribute,
      _idmPropertyName,
      sendOnPasswordChange
    } = this.state;

    const isNew = this._getIsNew();
    const attribute = isNew ? this.state.attribute : _attribute;
    if (!attribute) {
      return null;
    }
    const forceSearchParameters = new Domain.SearchParameters().setFilter('objectClassId',
     attribute && attribute.objectClassId ? attribute.objectClassId : Domain.SearchParameters.BLANK_UUID);
    const controlledValuesForceSearchParameters = new Domain.SearchParameters()
      .setFilter('attributeMappingId', attribute.id).setFilter('historicValue', false);
    const historicValuesForceSearchParameters = new Domain.SearchParameters()
      .setFilter('attributeMappingId', attribute.id).setFilter('historicValue', true);
    const overriddenForceSearchParameters = new Domain.SearchParameters().setFilter('systemAttributeMappingId', attribute.id);
    const _isDisabled = disabledAttribute;
    const _isEntityAttribute = entityAttribute;
    const _isExtendedAttribute = extendedAttribute;
    const _showNoRepositoryAlert = (!_isExtendedAttribute && !_isEntityAttribute);

    const entityTypeEnum = SystemEntityTypeEnum.getEntityEnum(_systemMapping ? _systemMapping.entityType : 'IDENTITY');
    const _idmPropertyNameKey = _idmPropertyName !== undefined ? _idmPropertyName : attribute.idmPropertyName;
    const propertyHelpBlockLabel = _idmPropertyNameKey && entityTypeEnum.getEnum(_idmPropertyNameKey) ?
      entityTypeEnum.getHelpBlockLabel(
        entityTypeEnum.findKeyBySymbol(
          entityTypeEnum.getEnum(_idmPropertyNameKey)
        )
      ) : '';
    const _isRequiredIdmField = (_isEntityAttribute || _isExtendedAttribute) && !_isDisabled && !passwordAttribute;
    const isSynchronization = !!(_systemMapping && _systemMapping.operationType && _systemMapping.operationType === 'SYNCHRONIZATION');
    const strategyTypeTemp = strategyType || attribute.strategyType;
    const isMerge = strategyTypeTemp === AttributeMappingStrategyTypeEnum.findKeyBySymbol(AttributeMappingStrategyTypeEnum.MERGE);
    const showPasswordFilter = passwordAttribute && !isSynchronization && _systemMapping && _systemMapping.entityType === 'IDENTITY';

    return (
      <div>
        <Helmet title={this.i18n('title')}/>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Basic.ContentHeader>
          <Basic.Icon value="list-alt"/>
          {' '}
          <span dangerouslySetInnerHTML={{__html: this.i18n('header', attribute ? {name: attribute.idmPropertyName} : {})}}/>
        </Basic.ContentHeader>
        <Basic.Tabs
          activeKey={activeKey}
          onSelect={this._onChangeSelectTabs.bind(this)}>
          <Basic.Tab eventKey={1} title={this.i18n('tabs.basic.label')} className="bordered">
            <form onSubmit={this.save.bind(this)}>
              <Basic.Panel className="no-border last" style={{marginBottom: 0, paddingRight: 15, paddingLeft: 15, paddingTop: 15}}>
                <Basic.AbstractForm
                  ref="form"
                  data={attribute}
                  showLoading={_showLoading}
                  readOnly={!Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])}>
                  <Basic.Checkbox
                    ref="disabledAttribute"
                    onChange={this._checkboxChanged.bind(this, 'disabledAttribute', null)}
                    tooltip={this.i18n('acc:entity.SystemAttributeMapping.disabledAttribute.tooltip')}
                    label={this.i18n('acc:entity.SystemAttributeMapping.disabledAttribute.label')}/>
                  <Basic.SelectBox
                    ref="systemMapping"
                    hidden
                    manager={systemMappingManager}
                    label={this.i18n('acc:entity.SystemAttributeMapping.systemMapping')}
                    readOnly
                    required/>
                  <Basic.SelectBox
                    ref="schemaAttribute"
                    manager={schemaAttributeManager}
                    forceSearchParameters={forceSearchParameters}
                    onChange={this._schemaAttributeChange.bind(this)}
                    label={this.i18n('acc:entity.SystemAttributeMapping.schemaAttribute')}
                    readOnly={_isDisabled}
                    required
                    pageSize={Domain.SearchParameters.MAX_SIZE}/>
                  <Basic.TextField
                    ref="name"
                    label={this.i18n('acc:entity.SystemAttributeMapping.name.label')}
                    helpBlock={this.i18n('acc:entity.SystemAttributeMapping.name.help')}
                    readOnly={_isDisabled}
                    required
                    max={255}/>
                  <Basic.Checkbox
                    ref="uid"
                    onChange={this._checkboxChanged.bind(this, 'uid', null)}
                    tooltip={this.i18n('acc:entity.SystemAttributeMapping.uid.tooltip')}
                    label={this.i18n('acc:entity.SystemAttributeMapping.uid.label')}
                    readOnly={_isDisabled}/>
                  <Basic.Checkbox
                    ref="entityAttribute"
                    onChange={this._checkboxChanged.bind(this, 'entityAttribute', 'extendedAttribute')}
                    label={this.i18n('acc:entity.SystemAttributeMapping.entityAttribute')}
                    readOnly={_isDisabled || passwordAttribute}/>
                  <Basic.Checkbox
                    ref="extendedAttribute"
                    onChange={this._checkboxChanged.bind(this, 'extendedAttribute', 'entityAttribute')}
                    label={this.i18n('acc:entity.SystemAttributeMapping.extendedAttribute.label')}
                    readOnly={_isDisabled || passwordAttribute}
                    helpBlock={this.i18n('acc:entity.SystemAttributeMapping.extendedAttribute.help', {escape: false})}/>
                  <Basic.Row>
                    <div className="col-lg-6">
                      <Basic.EnumSelectBox
                        ref="idmPropertyEnum"
                        readOnly={_isDisabled || !_isEntityAttribute || passwordAttribute}
                        enum={entityTypeEnum}
                        helpBlock={this.i18n(`acc:${propertyHelpBlockLabel}`, {escape: false})}
                        onChange={this._onChangeEntityEnum.bind(this)}
                        label={this.i18n('acc:entity.SystemAttributeMapping.idmPropertyEnum')}
                      />
                    </div>
                    <div className="col-lg-6">
                      <Basic.TextField
                        ref="idmPropertyName"
                        readOnly={_isDisabled || !_isRequiredIdmField || _isEntityAttribute || passwordAttribute}
                        label={this.i18n('acc:entity.SystemAttributeMapping.idmPropertyName.label')}
                        helpBlock={this.i18n('acc:entity.SystemAttributeMapping.idmPropertyName.help')}
                        required={_isRequiredIdmField}
                        max={255}/>
                    </div>
                  </Basic.Row>
                  <Basic.EnumSelectBox
                    ref="strategyType"
                    enum={AttributeMappingStrategyTypeEnum}
                    onChange={this._strategyTypeChange.bind(this)}
                    label={this.i18n('acc:entity.SystemAttributeMapping.strategyType')}
                    readOnly={_isDisabled}
                    required/>
                  <Basic.Checkbox
                    ref="sendAlways"
                    hidden={isSynchronization}
                    tooltip={this.i18n('acc:entity.SystemAttributeMapping.sendAlways.tooltip')}
                    label={this.i18n('acc:entity.SystemAttributeMapping.sendAlways.label')}
                    readOnly={_isDisabled}/>
                  <Basic.Checkbox
                    ref="sendOnlyIfNotNull"
                    hidden={isSynchronization}
                    tooltip={this.i18n('acc:entity.SystemAttributeMapping.sendOnlyIfNotNull.tooltip')}
                    label={this.i18n('acc:entity.SystemAttributeMapping.sendOnlyIfNotNull.label')}
                    readOnly={_isDisabled}/>
                  <Basic.Checkbox
                    ref="confidentialAttribute"
                    label={this.i18n('acc:entity.SystemAttributeMapping.confidentialAttribute')}
                    readOnly={_isDisabled || !_isRequiredIdmField}/>
                  <Basic.Checkbox
                    ref="authenticationAttribute"
                    label={this.i18n('acc:entity.SystemAttributeMapping.authenticationAttribute.label')}
                    helpBlock={this.i18n('acc:entity.SystemAttributeMapping.authenticationAttribute.help')}
                    readOnly={_isDisabled}/>
                  <Basic.Div style={{display: 'flex'}}>
                    <Basic.Div style={{flex: 5}}>
                      <Basic.Checkbox
                        ref="sendOnPasswordChange"
                        onChange={this._checkboxChanged.bind(this, 'sendOnPasswordChange', null)}
                        label={this.i18n('acc:entity.SystemAttributeMapping.sendOnPasswordChange.label')}
                        helpBlock={this.i18n('acc:entity.SystemAttributeMapping.sendOnPasswordChange.help')}
                        readOnly={_isDisabled}/>
                    </Basic.Div>
                    <Basic.Div style={{flex: 10}}>
                      <Basic.Checkbox
                        ref="sendOnlyOnPasswordChange"
                        hidden={!sendOnPasswordChange}
                        label={this.i18n('acc:entity.SystemAttributeMapping.sendOnlyOnPasswordChange.label')}
                        helpBlock={this.i18n('acc:entity.SystemAttributeMapping.sendOnlyOnPasswordChange.help')}
                        readOnly={_isDisabled}/>
                    </Basic.Div>
                  </Basic.Div>
                  <Basic.Checkbox
                    ref="passwordAttribute"
                    hidden={isSynchronization}
                    onChange={this._checkboxChanged.bind(this, 'passwordAttribute', null)}
                    label={this.i18n('acc:entity.SystemAttributeMapping.passwordAttribute.label')}
                    helpBlock={this.i18n('acc:entity.SystemAttributeMapping.passwordAttribute.help')}
                    readOnly={_isDisabled}/>
                  <Basic.Checkbox
                    ref="cached"
                    label={this.i18n('acc:entity.SystemAttributeMapping.cached.label')}
                    helpBlock={this.i18n('acc:entity.SystemAttributeMapping.cached.help', {escape: false})}
                    readOnly={_isDisabled}/>
                  <Basic.LabelWrapper label=" ">
                    <Basic.Alert
                      rendered={_showNoRepositoryAlert && !passwordAttribute}
                      key="no-repository-alert"
                      icon="exclamation-sign"
                      className="no-margin"
                      text={this.i18n('alertNoRepository')}/>
                    <Basic.Alert
                      rendered={passwordAttribute === true}
                      key="password-info-alert"
                      icon="exclamation-sign"
                      className="no-margin"
                      text={this.i18n('infoPasswordMapping')}/>
                  </Basic.LabelWrapper>
                  <Advanced.ScriptArea
                    ref="transformFromResourceScript"
                    completers={this._getCompletersFromResource()}
                    scriptCategory={[Enums.ScriptCategoryEnum.findKeyBySymbol(Enums.ScriptCategoryEnum.TRANSFORM_FROM),
                      Enums.ScriptCategoryEnum.findKeyBySymbol(Enums.ScriptCategoryEnum.DEFAULT)]}
                    headerText={this.i18n('acc:entity.SystemAttributeMapping.transformFromResourceScriptSelectBox.label')}
                    label={this.i18n('acc:entity.SystemAttributeMapping.transformFromResourceScript.label')}
                    helpBlock={this.i18n('acc:entity.SystemAttributeMapping.transformFromResourceScript.help', {escape: false})}
                    scriptManager={scriptManager}
                    readOnly={_isDisabled}/>
                  <Advanced.ScriptArea
                    ref="transformToResourceScript"
                    rendered={!isMerge}
                    completers={this._getCompletersToResource()}
                    scriptCategory={[Enums.ScriptCategoryEnum.findKeyBySymbol(Enums.ScriptCategoryEnum.DEFAULT),
                      Enums.ScriptCategoryEnum.findKeyBySymbol(Enums.ScriptCategoryEnum.TRANSFORM_TO)]}
                    headerText={this.i18n('acc:entity.SystemAttributeMapping.transformToResourceScriptSelectBox.label')}
                    helpBlock={this.i18n('acc:entity.SystemAttributeMapping.transformToResourceScript.help', {escape: false})}
                    label={this.i18n('acc:entity.SystemAttributeMapping.transformToResourceScript.label')}
                    scriptManager={scriptManager}
                    readOnly={_isDisabled}/>
                </Basic.AbstractForm>
                <Basic.PanelFooter rendered={!this.isWizard()}>
                  {this.renderButtons(_showLoading)}
                </Basic.PanelFooter>
              </Basic.Panel>
            </form>
          </Basic.Tab>
          <Basic.Tab eventKey={2} rendered={isMerge && !isNew} title={this.i18n('tabs.controlledValues.label')} className="bordered">
            <Basic.ContentHeader
              text={this.i18n('tabs.controlledValues.cache.header')}
              style={{marginBottom: 0, paddingTop: 15, paddingRight: 15, paddingLeft: 15}}/>
            <Basic.Alert
              level="info"
              showHtmlText
              text={this.i18n('tabs.controlledValues.cache.helpBlock')}
              style={{marginBottom: 0, marginRight: 15, marginLeft: 15}}
            />
            <Basic.Alert
              level="warning"
              rendered={attribute && attribute.evictControlledValuesCache}
              showHtmlText
              text={this.i18n('tabs.controlledValues.cache.evicted')}
              style={{marginBottom: 0, marginRight: 15, marginLeft: 15}}
            />
            <AttributeControlledValueTable
              ref="controlledValuesTable"
              uiKey={`attribute-mapping-controlled-values${attribute.id}`}
              showRowSelection={false}
              manager={controlledValueManager}
              forceSearchParameters={controlledValuesForceSearchParameters}
            />
            <Basic.ContentHeader
              text={this.i18n('tabs.controlledValues.historic.header')}
              style={{marginBottom: 0, paddingTop: 15, paddingRight: 15, paddingLeft: 15}}/>
            <Basic.Alert
              level="info"
              showHtmlText
              text={this.i18n('tabs.controlledValues.historic.helpBlock')}
              style={{marginBottom: 0, marginRight: 15, marginLeft: 15}}
            />
            <AttributeControlledValueTable
              ref="historicValuesTable"
              uiKey={`attribute-mapping-historic-values${attribute.id}`}
              manager={controlledValueManager}
              forceSearchParameters={historicValuesForceSearchParameters}
            />
          </Basic.Tab>
          <Basic.Tab eventKey={3} rendered={!isNew} title={this.i18n('tabs.attributeOverridden.label')} className="bordered">
            <Basic.ContentHeader
              text={this.i18n('tabs.attributeOverridden.header')}
              style={{marginBottom: 0, paddingTop: 15, paddingRight: 15, paddingLeft: 15}}/>
            <RoleSystemAttributeTable
              uiKey={`attribute-mapping-overridden-${attribute.id}`}
              rendered={activeKey === 3}
              columns={['role', 'transformScript', 'strategyType']}
              linkMenu={null}
              roleSystem={null}
              readOnly
              isSystemMenu={false}
              showAddButton={false}
              showFilter={false}
              showRowSelection={false}
              forceSearchParameters={overriddenForceSearchParameters}
              match={this.props.match}/>
          </Basic.Tab>
          <Basic.Tab eventKey={4} rendered={showPasswordFilter} title={this.i18n('tabs.passwordFilter.label')} className="bordered">
            <form onSubmit={this.save.bind(this)}>
              <Basic.Panel className="no-border last" style={{marginBottom: 0, paddingRight: 15, paddingLeft: 15, paddingTop: 15}}>
                <Basic.AbstractForm
                  ref="formPasswordFilter"
                  data={attribute}
                  showLoading={_showLoading}
                  readOnly={!Managers.SecurityManager.hasAnyAuthority(['SYSTEMATTRIBUTEMAPPING_UPDATE'])}>
                  <Basic.Checkbox
                    ref="passwordFilter"
                    label={this.i18n('acc:entity.SystemAttributeMapping.passwordFilter.passwordFilter.label')}
                    helpBlock={this.i18n('acc:entity.SystemAttributeMapping.passwordFilter.passwordFilter.help')}/>
                  <Basic.TextField
                    ref="echoTimeout"
                    label={this.i18n('acc:entity.SystemAttributeMapping.passwordFilter.echoTimeout.label')}
                    helpBlock={this.i18n('acc:entity.SystemAttributeMapping.passwordFilter.echoTimeout.help')}
                    type="number"
                    validation={Joi.number()
                      .allow(null)
                      .integer()
                      .min(1)
                      .max(2147483647)}
                    required/>
                  <Advanced.ScriptArea
                    ref="transformationUidScript"
                    scriptCategory={[Enums.ScriptCategoryEnum.findKeyBySymbol(Enums.ScriptCategoryEnum.SYSTEM),
                      Enums.ScriptCategoryEnum.findKeyBySymbol(Enums.ScriptCategoryEnum.SYSTEM)]}
                    headerText={this.i18n('acc:entity.SystemAttributeMapping.passwordFilter.transformationUidScript.label')}
                    helpBlock={this.i18n('acc:entity.SystemAttributeMapping.passwordFilter.transformationUidScript.help')}
                    label={this.i18n('acc:entity.SystemAttributeMapping.passwordFilter.transformationUidScript.label')}
                    scriptManager={this.scriptManager}/>
                </Basic.AbstractForm>
                <Basic.PanelFooter rendered={!this.isWizard()}>
                  <Basic.Button
                    type="button"
                    level="link"
                    onClick={this.goBack.bind(this)}
                    showLoading={_showLoading}>
                    {this.i18n('button.back')}
                  </Basic.Button>
                  <Basic.Button
                    level="success"
                    type="submit"
                    showLoading={_showLoading}
                    rendered={Managers.SecurityManager.hasAnyAuthority(['SYSTEMATTRIBUTEMAPPING_UPDATE'])}>
                    {this.i18n('button.save')}
                  </Basic.Button>
                </Basic.PanelFooter>
              </Basic.Panel>
            </form>
          </Basic.Tab>
        </Basic.Tabs>
      </div>
    );
  }
}

SystemAttributeMappingDetail.propTypes = {
  _showLoading: PropTypes.bool,
};
SystemAttributeMappingDetail.defaultProps = {
  _showLoading: false,
};

function select(state, component) {
  const entity = Utils.Entity.getEntity(state, manager.getEntityType(), component.match.params.attributeId);
  let systemMapping = null;
  if (component && component.location && component.location.query && component.location.query.new) {
    systemMapping = Utils.Entity.getEntity(state, systemMappingManager.getEntityType(), component.location.query.mappingId);
  }
  if (entity) {
    systemMapping = entity._embedded && entity._embedded.systemMapping ? entity._embedded.systemMapping : null;
    const schemaAttribute = entity._embedded && entity._embedded.schemaAttribute ? entity._embedded.schemaAttribute : null;
    entity.systemMapping = systemMapping;
    entity.schemaAttribute = schemaAttribute;
    entity.objectClassId = schemaAttribute ? schemaAttribute.objectClass : Domain.SearchParameters.BLANK_UUID;
    entity.idmPropertyEnum = SystemEntityTypeEnum.getEntityEnum(systemMapping ? systemMapping.entityType : 'IDENTITY')
      .getEnum(entity.idmPropertyName);
  }
  return {
    _attribute: entity,
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
    _systemMapping: systemMapping
  };
}

export default connect(select)(SystemAttributeMappingDetail);
