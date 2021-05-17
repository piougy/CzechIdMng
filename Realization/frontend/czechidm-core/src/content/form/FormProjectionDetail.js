import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';
import _ from 'lodash';
import Joi from 'joi';
//
import * as Utils from '../../utils';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Domain from '../../domain';
import { FormProjectionManager, FormDefinitionManager, FormAttributeManager, DataManager } from '../../redux';
import AbstractEnum from '../../enums/AbstractEnum';

const manager = new FormProjectionManager();
const formDefinitionManager = new FormDefinitionManager();
const formAttributeManager = new FormAttributeManager();

/**
 * Modified IdentityAttributeEnum - singular properties
 *
 * TODO: DRY, but how to generalize enum + static methods ...
 *
 * @author Radek Tomiška
 */
class IdentityAttributeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.IdentityAttributeEnum.${ key }`);
  }

  static getHelpBlockLabel(key) {
    return super.getNiceLabel(`core:enums.IdentityAttributeEnum.helpBlock.${ key }`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }

  static getField(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.USERNAME: {
        return 'username';
      }
      case this.EXTERNAL_CODE: {
        return 'externalCode';
      }
      case this.DISABLED: {
        return 'disabled';
      }
      case this.FIRSTNAME: {
        return 'firstName';
      }
      case this.LASTNAME: {
        return 'lastName';
      }
      case this.EMAIL: {
        return 'email';
      }
      case this.PHONE: {
        return 'phone';
      }
      case this.TITLE_BEFORE: {
        return 'titleBefore';
      }
      case this.TITLE_AFTER: {
        return 'titleAfter';
      }
      case this.DESCRIPTION: {
        return 'description';
      }
      case this.STATE: {
        return 'state';
      }
      case this.PASSWORD: {
        return 'password';
      }
      default: {
        return null;
      }
    }
  }

  static getEnum(field) {
    if (!field) {
      return null;
    }

    switch (field) {
      case 'username': {
        return this.USERNAME;
      }
      case 'externalCode': {
        return this.EXTERNAL_CODE;
      }
      case 'disabled': {
        return this.DISABLED;
      }
      case 'firstName': {
        return this.FIRSTNAME;
      }
      case 'lastName': {
        return this.LASTNAME;
      }
      case 'email': {
        return this.EMAIL;
      }
      case 'phone': {
        return this.PHONE;
      }
      case 'titleBefore': {
        return this.TITLE_BEFORE;
      }
      case 'titleAfter': {
        return this.TITLE_AFTER;
      }
      case 'description': {
        return this.DESCRIPTION;
      }
      case 'state': {
        return this.STATE;
      }
      case 'password': {
        return this.PASSWORD;
      }
      default: {
        return null;
      }
    }
  }

  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      default: {
        return 'default';
      }
    }
  }
}

IdentityAttributeEnum.USERNAME = Symbol('USERNAME');
IdentityAttributeEnum.EXTERNAL_CODE = Symbol('EXTERNAL_CODE');
IdentityAttributeEnum.DISABLED = Symbol('DISABLED');
IdentityAttributeEnum.FIRSTNAME = Symbol('FIRSTNAME');
IdentityAttributeEnum.LASTNAME = Symbol('LASTNAME');
IdentityAttributeEnum.EMAIL = Symbol('EMAIL');
IdentityAttributeEnum.PHONE = Symbol('PHONE');
IdentityAttributeEnum.TITLE_BEFORE = Symbol('TITLE_BEFORE');
IdentityAttributeEnum.TITLE_AFTER = Symbol('TITLE_AFTER');
IdentityAttributeEnum.DESCRIPTION = Symbol('DESCRIPTION');
IdentityAttributeEnum.STATE = Symbol('STATE');
IdentityAttributeEnum.PASSWORD = Symbol('PASSWORD');

/**
 * Modified ContractAttributeEnum - singular properties
 *
 * TODO: DRY, but how to generalize enum + static methods ...
 *
 * @author Radek Tomiška
 * @since 11.0.0
 */
class ContractAttributeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.ContractAttributeEnum.${ key }`);
  }

  static getHelpBlockLabel(key) {
    return super.getNiceLabel(`core:enums.ContractAttributeEnum.helpBlock.${ key }`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }

  static getField(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.VALID_FROM: {
        return 'validFrom';
      }
      case this.VALID_TILL: {
        return 'validTill';
      }
      case this.WORK_POSITION: {
        return 'workPosition';
      }
      /* TODO: change script cannot be created (java init is needed to maintain backward compatibility)
      case this.POSITION: {
        return 'position';
      }
      case this.EXTERNE: {
        return 'externe';
      }
      case this.MAIN: {
        return 'main';
      }
      case this.DESCRIPTION: {
        return 'description';
      }
      case this.STATE: {
        return 'state';
      } */
      default: {
        return null;
      }
    }
  }

  static getEnum(field) {
    if (!field) {
      return null;
    }

    switch (field) {
      case 'validFrom': {
        return this.VALID_FROM;
      }
      case 'validTill': {
        return this.VALID_TILL;
      }
      case 'workPosition': {
        return this.WORK_POSITION;
      }
      /* TODO: change script cannot be created (java init is needed to maintain backward compatibility)
      case 'position': {
        return this.POSITION;
      }
      case 'externe': {
        return this.EXTERNE;
      }
      case 'main': {
        return this.MAIN;
      }
      case 'description': {
        return this.DESCRIPTION;
      }
      case 'disabled': {
        return this.DISABLED;
      }
      case 'state': {
        return this.STATE;
      }*/
      default: {
        return null;
      }
    }
  }

  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      default: {
        return 'default';
      }
    }
  }
}

// TODO: change script cannot be created (java init is needed to maintain backward compatibility)
// ContractAttributeEnum.MAIN = Symbol('MAIN');
// ContractAttributeEnum.STATE = Symbol('STATE');
// ContractAttributeEnum.POSITION = Symbol('POSITION');
ContractAttributeEnum.WORK_POSITION = Symbol('WORK_POSITION');
ContractAttributeEnum.VALID_FROM = Symbol('VALID_FROM');
ContractAttributeEnum.VALID_TILL = Symbol('VALID_TILL');
// ContractAttributeEnum.EXTERNE = Symbol('EXTERNE');
// ContractAttributeEnum.DESCRIPTION = Symbol('DESCRIPTION');

/**
* Form projection detail.
*
* @author Radek Tomiška
* @since 10.2.0
*/
class FormProjectionDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      detail: {
        show: false,
        isNew: true,
        entity: {}
      },
      formValidations: new Immutable.OrderedMap(),
      basicFields: [],
      basicContractFields: [],
      validateFormAttribute: null
    };
  }

  getContentKey() {
    return 'content.form-projections';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    const { isNew, entity } = this.props;
    //
    if (isNew) {
      this._initProjection(entity || {
        route: '/form/identity-projection',
        ownerType: 'eu.bcvsolutions.idm.core.model.entity.IdmIdentity'
      });
    } else {
      this.getLogger().debug(`[FormDetail] loading entity detail [id:${ entityId }]`);
      this.context.store.dispatch(manager.fetchEntity(entityId, null, (projection) => {
        this._initProjection(projection);
      }));
    }
    this.context.store.dispatch(manager.fetchSupportedRoutes());
  }

  _initProjection(projection) {
    const { supportedRoutes } = this.props;
    let _formDefinitions = new Immutable.OrderedMap();
    let _formValidations = new Immutable.OrderedMap();
    //
    if (projection) {
      // basic fields
      try {
        if (projection.basicFields) {
          const allFields = JSON.parse(projection.basicFields);
          projection.basicFields = allFields
            .filter(f => f.indexOf('.') < 0 || f.indexOf('IdmIdentity.') > -1)
            .map(f => IdentityAttributeEnum.findKeyBySymbol(IdentityAttributeEnum.getEnum(f)));
          projection.basicContractFields = allFields
            .filter(f => f.indexOf('IdmIdentityContract.') > -1)
            .map(f => f.replace('IdmIdentityContract.', ''))
            .map(f => ContractAttributeEnum.findKeyBySymbol(ContractAttributeEnum.getEnum(f)));
        }
      } catch (syntaxError) {
        // nothing
      }
      // form definitions and attributes
      try {
        // immutable map
        const formDefinitions = JSON.parse(projection.formDefinitions);
        if (formDefinitions && formDefinitions.length > 0) {
          formDefinitions.forEach(formDefinition => {
            if (formDefinition.definition) {
              _formDefinitions = _formDefinitions.set(formDefinition.definition, new Immutable.OrderedSet());
              if (formDefinition.attributes) {
                formDefinition.attributes.forEach(attribute => {
                  _formDefinitions = _formDefinitions.set(formDefinition.definition, _formDefinitions.get(formDefinition.definition).add(attribute));
                });
              }
            }
          });
        }
      } catch (syntaxError) {
        // nothing
      }
      //
      // form validations
      try {
        // immutable map
        const formValidations = JSON.parse(projection.formValidations);
        if (formValidations && formValidations.length > 0) {
          formValidations.forEach(formValidation => {
            // id is mixed => code + uuid for attributes from form definition
            if (!formValidation.id) {
              formValidation.id = formValidation.basicField;
            }
            _formValidations = _formValidations.set(formValidation.id, formValidation);
          });
        }
      } catch (syntaxError) {
        // nothing
      }
    }
    //
    this.setState({
      projection,
      basicFields: projection.basicFields,
      basicContractFields: projection.basicContractFields,
      formDefinitions: _formDefinitions,
      formValidations: _formValidations,
      formProjectionRoute: !supportedRoutes || !supportedRoutes.has(projection.route) ? null : supportedRoutes.get(projection.route)
    }, () => {
      this.refs.code.focus();
    });
  }

  getNavigationKey() {
    return 'form-projection-detail';
  }

  save(event) {
    const { uiKey } = this.props;
    const { formDefinitions, formValidations } = this.state;
    //
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    if (this.refs.formInstance) {
      if (!this.refs.formInstance.isValid()) {
        return;
      }
    }
    //
    this.refs.form.processStarted();
    const entity = this.refs.form.getData();
    // basic fields => identity + contract
    const allBasicFields = [];
    if (entity.basicFields) {
      entity.basicFields.forEach(f => allBasicFields.push(IdentityAttributeEnum.getField(IdentityAttributeEnum.findSymbolByKey(f))));
    }
    if (entity.basicContractFields) {
      entity.basicContractFields.forEach(f => {
        allBasicFields.push(`IdmIdentityContract.${ ContractAttributeEnum.getField(ContractAttributeEnum.findSymbolByKey(f)) }`);
      });
    }
    if (allBasicFields.length === 0) {
      entity.basicFields = null;
    } else {
      entity.basicFields = JSON.stringify(allBasicFields);
    }
    // form definitions and attributes
    const _formDefinitions = [];
    formDefinitions.forEach((formDefinition, definitionId) => {
      if (definitionId) {
        _formDefinitions.push({
          definition: definitionId,
          attributes: formDefinitions.get(definitionId).toArray()
        });
      }
    });
    entity.formDefinitions = JSON.stringify(_formDefinitions);
    entity.formValidations = JSON.stringify(formValidations.toArray().map(formValidation => {
      const _formValidation = _.merge({}, formValidation); // copy => id cannot be cleared in original instance
      if (_formValidation.basicField) {
        _formValidation.id = null;
      }
      return _formValidation;
    }));
    //
    // transform properties
    if (this.refs.formInstance) {
      entity.properties = this.refs.formInstance.getProperties();
    }
    // save
    if (entity.id === undefined) {
      this.context.store.dispatch(manager.createEntity(entity, `${ uiKey }-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(manager.patchEntity(entity, `${ uiKey }-detail`, this._afterSave.bind(this)));
    }
  }

  /**
  * Just set showloading to false and set processEnded to form.
  * Call after save/create
  */
  _afterSave(entity, error) {
    const { isNew } = this.props;
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    //
    this.refs.form.processEnded();
    this.addMessage({ message: this.i18n('save.success', { code: entity.code }) });
    if (isNew) {
      this.context.history.replace(`/forms/form-projections`);
    }
  }

  showDetail(entity) {
    this.setState({
      detail: {
        show: true,
        isNew: Utils.Entity.isNew(entity),
        entity
      },
      validateFormAttribute: entity.basicField
    }, () => {
      // TODO: focus - selectbox s atributem dle definice
    });
  }

  closeDetail() {
    this.setState({
      detail: {
        show: false,
        isNew: true,
        entity: {}
      }
    });
  }

  onChangeBasicFields(basicFields) {
    this.setState({
      basicFields: basicFields.map(field => field.value)
    });
  }

  onChangeBasicContractFields(basicContractFields) {
    this.setState({
      basicContractFields: basicContractFields.map(field => field.value)
    });
  }

  onChangeFormDefinition(id, formDefinition) {
    const { formDefinitions } = this.state;
    //
    this.setState({
      formDefinitions: formDefinitions.remove(id).set(formDefinition.id, new Immutable.OrderedSet())
    });
  }

  onChangeFormAttributes(id, attributes) {
    const { formDefinitions } = this.state;
    //
    this.setState({
      formDefinitions: formDefinitions.set(id, new Immutable.OrderedSet(!attributes ? [] : attributes.map(a => a.id)))
    });
  }

  onChangeValidateFormAttribute(attribute) {
    this.setState({
      validateFormAttribute: attribute ? attribute.value : null
    });
  }

  addFormDefinition() {
    const { formDefinitions } = this.state;
    //
    this.setState({
      formDefinitions: formDefinitions.set(null, new Immutable.OrderedSet())
    });
  }

  removeFormDefinition(id) {
    const { formDefinitions } = this.state;
    //
    this.setState({
      formDefinitions: formDefinitions.remove(id)
    });
  }

  addFormValidation(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.formValidation.isFormValid()) {
      return;
    }
    const { formValidations, detail } = this.state;
    const entity = this.refs.formValidation.getData();
    const attribute = entity.id;
    entity.id = attribute.value; // attribute id or filed for basic fields
    entity.basicField = attribute.basicField; // only for FE
    entity.code = attribute.code;
    entity.name = attribute.name;
    entity.formDefinition = attribute.formDefinition;
    //
    if (detail.isNew && formValidations.has(entity.id)) {
      this.addMessage({ level: 'warning', message: this.i18n('entity.FormProjection.formValidations.create.message.contains') });
      return;
    }
    //
    this.setState({
      formValidations: formValidations.set(entity.id, entity)
    }, () => {
      this.closeDetail();
    });
  }

  removeFormValidation(id) {
    const { formValidations } = this.state;
    //
    this.setState({
      formValidations: formValidations.remove(id)
    });
  }

  onChangeRoute(formProjectionRoute) {
    this.setState({
      formProjectionRoute
    });
  }

  _getSupportedRoutes() {
    const { supportedRoutes } = this.props;
    //
    const _supportedRoutes = [];
    if (!supportedRoutes) {
      return [];
    }
    //
    supportedRoutes.forEach(route => {
      _supportedRoutes.push(this._toRouteOption(route));
    });
    //
    return _supportedRoutes;
  }

  _toRouteOption(route) {
    return {
      niceLabel: formAttributeManager.getLocalization(route.formDefinition, null, 'label', route.id),
      value: route.id,
      description: formAttributeManager.getLocalization(route.formDefinition, null, 'help', route.description),
      formDefinition: route.formDefinition
    };
  }

  render() {
    const { uiKey, showLoading, _permissions } = this.props;
    const { projection, formDefinitions, formValidations, formProjectionRoute, detail, validateFormAttribute } = this.state;
    //
    const _supportedRoutes = this._getSupportedRoutes();
    let formInstance = new Domain.FormInstance({});
    if (formProjectionRoute && formProjectionRoute.formDefinition && projection) {
      formInstance = new Domain.FormInstance(formProjectionRoute.formDefinition).setProperties(projection.properties);
    }
    //
    const showProperties = formInstance
      && formProjectionRoute
      && formProjectionRoute.formDefinition
      && formProjectionRoute.formDefinition.formAttributes.length > 0;
    //
    const _availableAttributes = [];
    for (const enumItem in IdentityAttributeEnum) {
      if (_.isSymbol(IdentityAttributeEnum[enumItem])) {
        if (enumItem === 'PASSWORD' || enumItem === 'STATE' || enumItem === 'DISABLED') {
          // this fields are not supported
          continue;
        }
        const field = IdentityAttributeEnum.getField(enumItem);
        _availableAttributes.push({
          value: field,
          code: field,
          name: `IdmIdentity.${ field }`,
          basicField: field,
          niceLabel: enumItem.niceLabel || IdentityAttributeEnum.getNiceLabel(enumItem)
        });
      }
    }
    for (const enumItem in ContractAttributeEnum) {
      if (_.isSymbol(ContractAttributeEnum[enumItem])) {
        const field = ContractAttributeEnum.getField(enumItem);
        _availableAttributes.push({
          value: `IdmIdentityContract.${ field }`,
          code: field,
          name: `IdmIdentityContract.${ field }`,
          basicField: `IdmIdentityContract.${ field }`,
          niceLabel: enumItem.niceLabel || ContractAttributeEnum.getNiceLabel(enumItem)
        });
      }
    }
    //
    return (
      <Basic.Div>
        <form onSubmit={ this.save.bind(this) }>
          <Basic.Panel className={ Utils.Entity.isNew(projection) ? '' : 'no-border last' }>
            <Basic.PanelHeader text={
              Utils.Entity.isNew(projection)
              ?
              this.i18n('create.header')
              :
              this.i18n('content.form-projections.detail.title')
            }/>
            <Basic.PanelBody style={ Utils.Entity.isNew(projection) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 } }>
              <Basic.AbstractForm
                ref="form"
                uiKey={ uiKey }
                data={ projection }
                readOnly={ !manager.canSave(projection, _permissions) }>
                <Basic.TextField
                  ref="ownerType"
                  label={ this.i18n('entity.FormProjection.ownerType.label') }
                  max={ 255 }
                  required
                  hidden/>
                <Basic.TextField
                  ref="code"
                  label={ this.i18n('entity.FormProjection.code.label') }
                  helpBlock={ this.i18n('entity.FormProjection.code.help') }
                  max={ 255 }
                  required/>
                <Basic.TextField
                  ref="module"
                  label={ this.i18n('entity.FormProjection.module.label') }
                  max={ 255 }
                  helpBlock={ this.i18n('entity.FormProjection.module.help') }/>
                <Basic.Row>
                  <Basic.Col lg={ 6 }>
                    <Basic.EnumSelectBox
                      enum={ IdentityAttributeEnum }
                      ref="basicFields"
                      label={ this.i18n('entity.FormProjection.basicFields.label') }
                      helpBlock={ this.i18n('entity.FormProjection.basicFields.help') }
                      placeholder={ this.i18n('entity.FormProjection.basicFields.placeholder') }
                      onChange={ this.onChangeBasicFields.bind(this) }
                      multiSelect/>
                  </Basic.Col>
                  <Basic.Col lg={ 6 }>
                    <Basic.EnumSelectBox
                      enum={ ContractAttributeEnum }
                      ref="basicContractFields"
                      label={ this.i18n('entity.FormProjection.basicContractFields.label') }
                      helpBlock={ this.i18n('entity.FormProjection.basicContractFields.help') }
                      placeholder={ this.i18n('entity.FormProjection.basicContractFields.placeholder') }
                      onChange={ this.onChangeBasicContractFields.bind(this) }
                      multiSelect/>
                  </Basic.Col>
                </Basic.Row>

                <Basic.LabelWrapper
                  label={ this.i18n('entity.FormProjection.formValidations.label') }
                  rendered={ _availableAttributes.length > 0 }>
                  {
                    formValidations.size === 0
                    ?
                    <Basic.Alert
                      text={ this.i18n('entity.FormProjection.formValidations.empty') }
                      className="no-margin"
                      buttons={[
                        <Basic.Button
                          level="info"
                          className="btn-xs"
                          icon="fa:plus"
                          title={ this.i18n('entity.FormProjection.formValidations.button.add.title') }
                          onClick={ this.showDetail.bind(this, {}) }
                          disabled={
                            !manager.canSave(projection, _permissions)
                          }>
                          { this.i18n('entity.FormProjection.formValidations.button.add.label') }
                        </Basic.Button>
                      ]}/>
                    :
                    <Basic.Div>
                      <Basic.Table
                        className="table-bordered"
                        data={ formValidations.toArray() }>
                        <Basic.Column
                          property=""
                          header=""
                          className="edit-button"
                          width={ 20 }
                          cell={
                            ({ rowIndex, data }) => (
                              <Basic.Button
                                title={ this.i18n('button.edit') }
                                buttonSize="xs"
                                icon="fa:pencil"
                                onClick={ this.showDetail.bind(this, data[rowIndex]) }/>
                            )
                          }/>
                        <Basic.Column
                          property="id"
                          header={ this.i18n('entity.FormAttribute._type') }
                          cell={
                            ({ rowIndex, data }) => {
                              const entity = data[rowIndex];
                              if (!entity.basicField) {
                                const attribute = formAttributeManager.getEntity(this.context.store.getState(), entity.id);
                                if (!attribute) {
                                  return entity.code;
                                }
                                return formAttributeManager.getNiceLabel(attribute, true);
                              }
                              if (!entity.basicField) {
                                return entity.code;
                              }
                              if (entity.basicField.indexOf('IdmIdentityContract.') > -1) {
                                return ContractAttributeEnum.getNiceLabel(
                                  ContractAttributeEnum.findKeyBySymbol(
                                    ContractAttributeEnum.getEnum(entity.basicField.replace('IdmIdentityContract.', ''))
                                  )
                                );
                              }
                              //
                              return IdentityAttributeEnum.getNiceLabel(
                                IdentityAttributeEnum.findKeyBySymbol(
                                  IdentityAttributeEnum.getEnum(entity.basicField.replace('IdmIdentity.', ''))
                                )
                              );
                            }
                          }/>
                        <Basic.Column
                          property="readonly"
                          header={<Basic.Cell className="column-face-bool">{this.i18n('entity.FormAttribute.readonly')}</Basic.Cell>}
                          cell={<Basic.BooleanCell className="column-face-bool"/>}
                          width={ 150 }/>
                        <Basic.Column
                          property="required"
                          header={<Basic.Cell className="column-face-bool">{this.i18n('entity.FormAttribute.required')}</Basic.Cell>}
                          cell={<Basic.BooleanCell className="column-face-bool"/>}
                          width={ 150 }/>
                        <Basic.Column
                          property="min"
                          header={ this.i18n('entity.FormAttribute.min.label') }/>
                        <Basic.Column
                          property="max"
                          header={ this.i18n('entity.FormAttribute.max.label') }/>
                        <Basic.Column
                          property="regex"
                          header={ this.i18n('entity.FormAttribute.regex.label') }/>
                        <Basic.Column
                          property=""
                          header=""
                          width={ 20 }
                          cell={
                            ({ rowIndex, data }) => (
                              <Basic.Button
                                level="danger"
                                buttonSize="xs"
                                icon="fa:trash"
                                title={ this.i18n('entity.FormProjection.formValidations.button.remove.title') }
                                titlePlacement="left"
                                onClick={ () => this.removeFormValidation(data[rowIndex].id) }
                                disabled={ !manager.canSave(projection, _permissions) }/>
                            )
                          }/>
                      </Basic.Table>

                      <Basic.Button
                        level="success"
                        className="btn-xs"
                        icon="fa:plus"
                        title={ this.i18n('entity.FormProjection.formValidations.button.add.title') }
                        style={{ marginBottom: 5, marginTop: 5 }}
                        onClick={ this.showDetail.bind(this, {}) }
                        disabled={
                          !manager.canSave(projection, _permissions)
                        }>
                        { this.i18n('entity.FormProjection.formValidations.button.add.label') }
                      </Basic.Button>
                    </Basic.Div>
                  }
                </Basic.LabelWrapper>

                <Basic.LabelWrapper
                  label={ this.i18n('entity.FormProjection.formDefinitions.label') }
                  helpBlock={ this.i18n('entity.FormProjection.formDefinitions.help') }>
                  {
                    !formDefinitions || formDefinitions.size === 0
                    ?
                    <Basic.Alert
                      text={ this.i18n('entity.FormProjection.formDefinitions.empty') }
                      className="no-margin"
                      buttons={[
                        <Basic.Button
                          level="info"
                          className="btn-xs"
                          icon="fa:plus"
                          title={ this.i18n('entity.FormProjection.formDefinitions.button.add.title') }
                          onClick={ () => this.addFormDefinition() }
                          disabled={
                            !manager.canSave(projection, _permissions)
                            ||
                            (formDefinitions && _.includes(formDefinitions.keySeq().toArray(), null))
                          }>
                          { this.i18n('entity.FormProjection.formDefinitions.button.add.label') }
                        </Basic.Button>
                      ]}/>
                    :
                    <Basic.Div>
                      {
                        [
                          ...formDefinitions.map((attributes, definitionId) => (
                            <Basic.Div style={{ display: 'flex' }}>
                              <Basic.Div style={{ flex: 2, paddingRight: 5 }}>
                                <Basic.SelectBox
                                  label={ null }
                                  manager={ formDefinitionManager }
                                  forceSearchParameters={
                                    new Domain.SearchParameters()
                                      .setFilter('type', [
                                        'eu.bcvsolutions.idm.core.model.entity.IdmIdentity',
                                        'eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract'
                                      ])
                                      .setSort('code')
                                  }
                                  value={ definitionId }
                                  placeholder={ this.i18n('entity.FormProjection.formDefinitions.placeholder') }
                                  style={{ marginBottom: 5 }}
                                  onChange={ (f) => this.onChangeFormDefinition(definitionId, f) }
                                  readOnly={ definitionId !== null }
                                  clearable={ false }/>
                              </Basic.Div>
                              <Basic.Div style={{ flex: 3, paddingRight: 5 }}>
                                <Basic.SelectBox
                                  label={ null }
                                  manager={ formAttributeManager }
                                  value={ attributes.toArray() }
                                  forceSearchParameters={ new Domain.SearchParameters().setFilter('definitionId', definitionId) }
                                  placeholder={ this.i18n('entity.FormProjection.formDefinitions.attributes.placeholder') }
                                  multiSelect
                                  style={{ marginBottom: 5 }}
                                  onChange={ (attrs) => this.onChangeFormAttributes(definitionId, attrs) }
                                  readOnly={ definitionId === null || !manager.canSave(projection, _permissions) }/>
                              </Basic.Div>
                              <Basic.Button
                                level="danger"
                                icon="fa:trash"
                                title={ this.i18n('entity.FormProjection.formDefinitions.button.remove.title') }
                                titlePlacement="left"
                                onClick={ () => this.removeFormDefinition(definitionId) }
                                disabled={ !manager.canSave(projection, _permissions) }/>
                            </Basic.Div>
                          )).values()
                        ]
                      }
                      <Basic.Button
                        level="success"
                        className="btn-xs"
                        icon="fa:plus"
                        title={ this.i18n('entity.FormProjection.formDefinitions.button.add.title') }
                        style={{ marginBottom: 5 }}
                        onClick={ () => this.addFormDefinition() }
                        disabled={
                          !manager.canSave(projection, _permissions)
                          ||
                          (formDefinitions && _.includes(formDefinitions.keySeq().toArray(), null))
                        }>
                        { this.i18n('entity.FormProjection.formDefinitions.button.add.label') }
                      </Basic.Button>
                    </Basic.Div>
                  }
                </Basic.LabelWrapper>

                <Basic.EnumSelectBox
                  ref="route"
                  options={ _supportedRoutes }
                  onChange={ this.onChangeRoute.bind(this) }
                  label={ this.i18n('entity.FormProjection.route.label') }
                  helpBlock={ formProjectionRoute ? formProjectionRoute.description : this.i18n('entity.FormProjection.route.help') }
                  clearable={ false }
                  required/>

                <Basic.Div style={ showProperties ? {} : { display: 'none' }}>
                  <Advanced.EavForm
                    ref="formInstance"
                    formInstance={ formInstance }
                    useDefaultValue={ Utils.Entity.isNew(projection) }
                    readOnly={ !manager.canSave(projection, _permissions) }
                    showAttributes/>
                </Basic.Div>

                <Basic.TextArea
                  ref="description"
                  label={ this.i18n('entity.FormProjection.description.label') }
                  rows={ 4 }
                  max={ 1000 }/>
                <Basic.Checkbox
                  ref="disabled"
                  label={ this.i18n('entity.FormProjection.disabled.label') }
                  helpBlock={ this.i18n('entity.FormProjection.disabled.help') }/>
              </Basic.AbstractForm>
            </Basic.PanelBody>
            <Basic.PanelFooter showLoading={ showLoading } >
              <Basic.Button type="button" level="link" onClick={ this.context.history.goBack }>
                { this.i18n('button.back') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={ showLoading }
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }
                rendered={ manager.canSave(projection, _permissions) }>
                { this.i18n('button.save') }
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
        </form>

        <Basic.Modal
          bsSize="large"
          show={ detail.show }
          onHide={ this.closeDetail.bind(this) }
          backdrop="static"
          keyboard>

          <form onSubmit={ this.addFormValidation.bind(this) }>
            <Basic.Modal.Header
              closeButton
              text={ this.i18n('entity.FormProjection.formValidations.create.header') }
              rendered={ detail.entity.id === undefined }/>
            <Basic.Modal.Header
              closeButton
              text={ this.i18n('entity.FormProjection.formValidations.edit.header', { name: detail.entity.name }) }
              rendered={ detail.entity.id !== undefined }/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="formValidation"
                data={ detail.entity }
                readOnly={ !manager.canSave(projection, _permissions) }>
                <Basic.EnumSelectBox
                  ref="id"
                  label={ this.i18n('entity.FormProjection.formValidations.attribute.label') }
                  helpBlock={ this.i18n('entity.FormProjection.formValidations.attribute.help') }
                  options={ _availableAttributes }
                  required
                  clearable={ false }
                  useObject
                  readOnly={ !detail.isNew }
                  onChange={ this.onChangeValidateFormAttribute.bind(this) }
                />
                <Basic.TextField
                  ref="label"
                  label={ this.i18n('entity.FormAttribute.name.label') }
                  helpBlock={ this.i18n('entity.FormAttribute.name.help') }
                  max={ 255 }/>
                <Basic.TextField
                  ref="placeholder"
                  label={ this.i18n('entity.FormAttribute.placeholder.label') }
                  helpBlock={ this.i18n('entity.FormAttribute.placeholder.help') }
                  max={ 255 }/>
                <Basic.Checkbox
                  ref="readonly"
                  label={ this.i18n('entity.FormAttribute.readonly') }/>
                <Basic.Checkbox
                  ref="required"
                  label={ this.i18n('entity.FormAttribute.required') }/>
                <Basic.TextField
                  ref="min"
                  label={
                    validateFormAttribute === 'IdmIdentityContract.validFrom' || validateFormAttribute === 'IdmIdentityContract.validTill'
                    ?
                    this.i18n('entity.FormAttribute.minDate.label')
                    :
                    this.i18n('entity.FormAttribute.minLength.label')
                  }
                  helpBlock={
                    validateFormAttribute === 'IdmIdentityContract.validFrom' || validateFormAttribute === 'IdmIdentityContract.validTill'
                    ?
                    this.i18n('entity.FormAttribute.minDate.help')
                    :
                    this.i18n('entity.FormAttribute.minLength.help')
                  }
                  validation={
                    Joi
                      .number()
                      .precision(4)
                      .min(-(10 ** 33))
                      .max(10 ** 33)
                      .allow(null)
                  }
                  readOnly={ !validateFormAttribute || validateFormAttribute === 'IdmIdentityContract.workPosition' }/>
                <Basic.TextField
                  ref="max"
                  label={
                    validateFormAttribute === 'IdmIdentityContract.validFrom' || validateFormAttribute === 'IdmIdentityContract.validTill'
                    ?
                    this.i18n('entity.FormAttribute.maxDate.label')
                    :
                    this.i18n('entity.FormAttribute.maxLength.label')
                  }
                  helpBlock={
                    validateFormAttribute === 'IdmIdentityContract.validFrom' || validateFormAttribute === 'IdmIdentityContract.validTill'
                    ?
                    this.i18n('entity.FormAttribute.maxDate.help')
                    :
                    this.i18n('entity.FormAttribute.maxLength.help')
                  }
                  validation={
                    Joi
                      .number()
                      .precision(4)
                      .min(-(10 ** 33))
                      .max(10 ** 33)
                      .allow(null)
                  }
                  readOnly={ !validateFormAttribute || validateFormAttribute === 'IdmIdentityContract.workPosition' }/>
                <Basic.TextField
                  ref="regex"
                  label={ this.i18n('entity.FormAttribute.regex.label') }
                  helpBlock={ this.i18n('entity.FormAttribute.regex.help') }
                  max={ 2000 }
                  readOnly={ !validateFormAttribute || validateFormAttribute.indexOf('IdmIdentityContract.') > -1 }/>
                <Basic.TextField
                  ref="validationMessage"
                  label={ this.i18n('entity.FormAttribute.validationMessage.label') }
                  helpBlock={ this.i18n('entity.FormAttribute.validationMessage.help') }
                  max={ 2000 } />
              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={ this.closeDetail.bind(this) }>
                { this.i18n('button.close') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                rendered={ manager.canSave(projection, _permissions) }>
                { this.i18n('button.set') }
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </Basic.Div>
    );
  }
}

FormProjectionDetail.propTypes = {
  uiKey: PropTypes.string,
  definitionManager: PropTypes.object,
  isNew: PropTypes.bool,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
FormProjectionDetail.defaultProps = {
  isNew: false,
  _permissions: null
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId),
    _permissions: manager.getPermissions(state, null, entityId),
    supportedRoutes: DataManager.getData(state, FormProjectionManager.UI_KEY_SUPPORTED_ROUTES)
  };
}

export default connect(select)(FormProjectionDetail);
