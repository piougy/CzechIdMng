import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Joi from 'joi';
import classnames from 'classnames';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import * as Domain from '../../domain';
import { FormAttributeManager, CodeListManager, DataManager } from '../../redux';
import PersistentTypeEnum from '../../enums/PersistentTypeEnum';
import ComponentService from '../../services/ComponentService';
//
const componentService = new ComponentService();
const manager = new FormAttributeManager();
const codeListManager = new CodeListManager();

/**
 * Form attribute detail
 *
 * FIXME: joi validation for precision does not work (see min 4.44444444 - schould fail)
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
class FormAttributeDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      _showLoading: false,
      persistentType: null
    };
  }

  getContentKey() {
    return 'content.formAttributes';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    const { isNew, formDefinitionId, entity } = this.props;
    //
    this.context.store.dispatch(manager.fetchSupportedAttributeRenderers());
    //
    if (isNew) {
      const _entity = entity || {
        persistentType: PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.SHORTTEXT),
        faceType: null,
        defaultValue: null,
        unmodifiable: false,
        formDefinition: formDefinitionId
      };
      //
      this.context.store.dispatch(manager.receiveEntity(entityId, _entity, null, () => {
        this.setState({
          persistentType: PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.SHORTTEXT),
          faceType: null,
          defaultValue: null
        }, () => {
          this.refs.codeable.focus();
        });
      }));
    } else {
      this.getLogger().debug(`[FormAttributeDetail] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(manager.fetchEntity(entityId, null, (_entity) => {
        this.setState({
          persistentType: _entity.persistentType,
          faceType: _entity.faceType,
          defaultValue: _entity.defaultValue
        }, () => {
          this.refs.codeable.focus();
        });
      }));
    }
  }

  getNavigationKey() {
    return 'forms-attribute-detail';
  }

  /**
   * Default save method that catch save event from form.
   */
  save(event) {
    const { uiKey, formDefinition } = this.props;
    const { persistentType } = this.state;
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
    this.setState({
      _showLoading: true
    }, () => {
      this.refs.form.processStarted();
      const entity = this.refs.form.getData();
      //
      // transform properties
      if (this.refs.formInstance) {
        entity.properties = this.refs.formInstance.getProperties();
      }
      const defaultValue = this.refs.defaultValue.getValue();
      if (defaultValue && defaultValue.value) {
        entity.defaultValue = defaultValue.value;
      } else {
        entity.defaultValue = null;
      }
      //
      const saveEntity = {
        ...entity,
        persistentType,
        faceType: persistentType === PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.CODELIST) ? entity.codeList : entity.faceType,
        code: entity.codeable.code,
        name: entity.codeable.name
      };

      if (entity.id === undefined) {
        saveEntity.formDefinition = formDefinition;
        this.context.store.dispatch(manager.createEntity(saveEntity, `${ uiKey }-detail`, (createdEntity, error) => {
          this._afterSave(createdEntity, error);
        }));
      } else {
        this.context.store.dispatch(manager.patchEntity(saveEntity, `${ uiKey }-detail`, this._afterSave.bind(this)));
      }
    });
  }

  _isUnmodifiable() {
    const { entity } = this.props;
    //
    return entity ? entity.unmodifiable : false;
  }

  /**
   * Method set showLoading to false and if is'nt error then show success message
   */
  _afterSave(entity, error) {
    if (error) {
      this.setState({
        _showLoading: false
      }, this.refs.form.processEnded());
      this.addError(error);
      return;
    }
    this.setState({
      _showLoading: false
    });
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    // FIXME: go back can be undefined
    this.context.history.goBack();
  }

  /**
   * Change persistent type listener.
   *
   * @param  {SelectBox.option} persistentType option from enum select box
   */
  onChangePersistentType(persistentType) {
    this.setState({
      persistentType: persistentType.value,
      faceType: null,
      defaultValue: null
    }, () => {
      // clear default value
      this.refs.defaultValue.setValue(null);
      // clear selected face type
      this.refs.faceType.setValue(null);
      // clear validations
      if (!this._supportsUniqueValidation(persistentType.value)) {
        this.refs.unique.setValue(false);
      }
      if (!this._supportsRegexValidation(persistentType.value)) {
        this.refs.regex.setValue(null);
      }
      if (!this._supportsMinMaxValidation(persistentType.value)) {
        this.refs.max.setValue(null);
        this.refs.min.setValue(null);
      }
    });
  }

  /**
   * Change face type listener
   *
   * @param  {SelectBox.option} faceType option from enum select box
   */
  onChangeFaceType(faceType) {
    this.setState({
      faceType: faceType ? faceType.value : null,
      defaultValue: null
    });
  }

  /**
   * Change codelist listener.
   *
   * @param  {object} codelist option from enum select box
   */
  onChangeCodeList(codelist) {
    this.setState({
      faceType: codelist ? codelist.code : null,
      defaultValue: null
    });
  }

  _supportsUniqueValidation(persistentType) {
    return this._supportsRegexValidation(persistentType);
  }

  _supportsRegexValidation(persistentType) {
    if (!persistentType) {
      return false;
    }
    return persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.BYTEARRAY)
        && persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.ATTACHMENT);
  }

  _supportsMinMaxValidation(persistentType) {
    if (!persistentType) {
      return false;
    }
    return persistentType === PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.DOUBLE)
        || persistentType === PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.INT)
        || persistentType === PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.LONG);
  }

  /**
   * Face types enum select box options by selected persistent type
   *
   * @return {arrayOf(SelectBox.option)}
   */
  getFaceTypes(persistentType) {
    if (!persistentType) {
      return null;
    }
    //
    const types = componentService.getComponentDefinitions(ComponentService.FORM_ATTRIBUTE_RENDERER)
      .filter(component => {
        if (!component.persistentType) {
          // persistent type is required
          return false;
        }
        // persistent type has to fit
        return component.persistentType === persistentType;
      })
      .toArray()
      .map(component => {
        const _faceType = component.faceType || component.persistentType;
        const _label = `${ component.labelKey ? this.i18n(component.labelKey) : _faceType }`;
        return {
          value: _faceType,
          niceLabel: `${ _label }${ _faceType === component.persistentType ? ` (${ this.i18n('label.default') })` : '' }`
        };
      });
    return types;
  }

  render() {
    const { entity, showLoading, _permissions, supportedAttributeRenderers } = this.props;
    const { _showLoading, persistentType, faceType, defaultValue } = this.state;
    //
    let formAttributeRenderer = null;
    if (faceType && supportedAttributeRenderers && supportedAttributeRenderers.has(faceType)) {
      formAttributeRenderer = supportedAttributeRenderers.get(faceType);
      if (formAttributeRenderer.disabled) {
        formAttributeRenderer = null;
      }
    }

    let formInstance = new Domain.FormInstance({});
    if (formAttributeRenderer && formAttributeRenderer.formDefinition && entity) {
      formInstance = new Domain.FormInstance(formAttributeRenderer.formDefinition).setProperties(entity.properties);
    }
    const showProperties = formInstance
      && formAttributeRenderer
      && formAttributeRenderer.formDefinition
      && formAttributeRenderer.formDefinition.formAttributes.length > 0;
    //
    const _attribute = entity ? { ...entity } : {};
    _attribute.name = this.i18n('entity.FormAttribute.defaultValue');
    _attribute.faceType = faceType;
    _attribute.persistentType = persistentType || PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.SHORTTEXT);
    //
    const component = manager.getFormComponent(_attribute);
    let FormValueComponent = null;
    let ManagerType = null;
    if (component) {
      FormValueComponent = component.component;
      ManagerType = component.manager;
    }
    //
    return (
      <Basic.Div>
        <form onSubmit={ this.save.bind(this) }>
          <Basic.Panel
            className={
              classnames({
                last: !Utils.Entity.isNew(entity),
                'no-border': !Utils.Entity.isNew(entity)
              })
            }>
            <Basic.PanelHeader text={ Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('content.formAttributes.detail.title') } />
            <Basic.PanelBody style={ Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 } }>
              <Basic.AbstractForm
                ref="form"
                data={ entity }
                showLoading={ showLoading || _showLoading }
                readOnly={ !manager.canSave(entity, _permissions) }>

                <Advanced.CodeableField
                  ref="codeable"
                  codeLabel={ this.i18n('entity.FormAttribute.code.label') }
                  codeHelpBlock={ this.i18n('entity.FormAttribute.code.help') }
                  codeReadOnly={ this._isUnmodifiable() }
                  nameLabel={ this.i18n('entity.FormAttribute.name.label') }
                  nameHelpBlock={ this.i18n('entity.FormAttribute.name.help') }/>

                <Basic.Row>
                  <Basic.Col lg={ 8 } className="col-lg-offset-4">
                    <Basic.TextField
                      ref="placeholder"
                      label={ this.i18n('entity.FormAttribute.placeholder.label') }
                      helpBlock={ this.i18n('entity.FormAttribute.placeholder.help') }
                      max={ 255 }/>
                  </Basic.Col>
                </Basic.Row>
                <Basic.Row>
                  <Basic.Col lg={ 4 } rendered={ persistentType !== null }>
                    <Basic.EnumSelectBox
                      enum={ PersistentTypeEnum }
                      readOnly={ this._isUnmodifiable() }
                      label={ this.i18n('entity.FormAttribute.persistentType') }
                      onChange={ this.onChangePersistentType.bind(this) }
                      useSymbol={ false }
                      required
                      clearable={ false }
                      value={{ value: persistentType, niceLabel: PersistentTypeEnum.getNiceLabel(persistentType) }}
                      searchable/>
                  </Basic.Col>
                  <Basic.Col lg={ 8 }>
                    <Basic.TextField
                      ref="seq"
                      label={ this.i18n('entity.FormAttribute.seq.label') }
                      helpBlock={ this.i18n('entity.FormAttribute.seq.help') }
                      validation={
                        Joi
                          .number()
                          .required()
                          .integer()
                          .min(-32768)
                          .max(32767)
                      }/>
                  </Basic.Col>
                </Basic.Row>
                <Basic.Row>
                  <Basic.Col lg={ 4 }>
                    <Basic.EnumSelectBox
                      ref="faceType"
                      options={ this.getFaceTypes(persistentType) }
                      label={ this.i18n('entity.FormAttribute.faceType.label') }
                      onChange={ this.onChangeFaceType.bind(this) }
                      helpBlock={ this.i18n('entity.FormAttribute.faceType.help') }
                      placeholder={ this.i18n('entity.FormAttribute.faceType.placeholder') }
                      hidden={ persistentType === PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.CODELIST) }
                      required={ persistentType === PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.ENUMERATION) }
                      searchable/>
                    <Basic.SelectBox
                      ref="codeList"
                      manager={ codeListManager }
                      label={ this.i18n('entity.FormAttribute.codeList.label') }
                      onChange={ this.onChangeCodeList.bind(this) }
                      helpBlock={ this.i18n('entity.FormAttribute.codeList.help') }
                      placeholder={ this.i18n('entity.FormAttribute.codeList.placeholder') }
                      hidden={ persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.CODELIST) }
                      required={ persistentType === PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.CODELIST) }
                      clearable={ false }
                      returnProperty="code"/>

                    <Basic.Div style={ showProperties ? {} : { display: 'none' }}>
                      <Advanced.EavForm
                        ref="formInstance"
                        formInstance={ formInstance }
                        useDefaultValue={ Utils.Entity.isNew(entity) }
                        readOnly={ !manager.canSave(entity, _permissions) }
                        showAttributes/>
                    </Basic.Div>

                  </Basic.Col>
                  <Basic.Col lg={ 8 }>
                    {
                      FormValueComponent && entity && persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.ATTACHMENT)
                      ?
                      <FormValueComponent
                        ref="defaultValue"
                        attribute={ _attribute }
                        useDefaultValue={ false }
                        values={[ {
                          value: defaultValue,
                          stringValue: defaultValue
                        }]}
                        manager={ ManagerType ? new ManagerType() : null }
                        component={ component }/>
                      :
                      <Basic.TextField
                        ref="defaultValue"
                        label={ this.i18n('entity.FormAttribute.defaultValue') }
                        max={ 255 }
                        readOnly={ persistentType === PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.ATTACHMENT) }/>
                    }
                  </Basic.Col>
                </Basic.Row>
                <Basic.Row>
                  <Basic.Col lg={ 12 }>
                    <Basic.TextArea
                      ref="description"
                      label={ this.i18n('entity.FormAttribute.description') }
                      max={ 2000 }/>
                  </Basic.Col>
                </Basic.Row>
                <Basic.Checkbox
                  ref="required"
                  readOnly={ this._isUnmodifiable() }
                  label={ this.i18n('entity.FormAttribute.required') }/>
                <Basic.Checkbox
                  ref="unique"
                  label={ this.i18n('entity.FormAttribute.unique.label') }
                  readOnly={ !this._supportsUniqueValidation(persistentType) }/>
                <Basic.TextField
                  ref="min"
                  label={ this.i18n('entity.FormAttribute.min.label') }
                  validation={
                    Joi
                      .number()
                      .precision(4)
                      .min(-(10 ** 33))
                      .max(10 ** 33)
                      .allow(null)
                  }
                  readOnly={ !this._supportsMinMaxValidation(persistentType) }/>
                <Basic.TextField
                  ref="max"
                  label={ this.i18n('entity.FormAttribute.max.label') }
                  validation={
                    Joi
                      .number()
                      .precision(4)
                      .min(-(10 ** 33))
                      .max(10 ** 33)
                      .allow(null)
                  }
                  readOnly={ !this._supportsMinMaxValidation(persistentType) }/>
                <Basic.TextField
                  ref="regex"
                  label={ this.i18n('entity.FormAttribute.regex.label') }
                  helpBlock={ this.i18n('entity.FormAttribute.regex.help') }
                  max={ 2000 }
                  readOnly={ !this._supportsRegexValidation(persistentType) }/>
                <Basic.TextField
                  ref="validationMessage"
                  label={ this.i18n('entity.FormAttribute.validationMessage.label') }
                  helpBlock={ this.i18n('entity.FormAttribute.validationMessage.help') }
                  max={ 2000 } />
                <Basic.Checkbox
                  ref="readonly"
                  readOnly={ this._isUnmodifiable() }
                  label={ this.i18n('entity.FormAttribute.readonly') }/>
                <Basic.Checkbox
                  ref="confidential"
                  readOnly={ this._isUnmodifiable() }
                  label={ this.i18n('entity.FormAttribute.confidential') }/>
                <Basic.Checkbox
                  ref="multiple"
                  readOnly={ this._isUnmodifiable() }
                  label={ this.i18n('entity.FormAttribute.multiple') }/>
                <Basic.Checkbox
                  ref="unmodifiable"
                  readOnly
                  label={ this.i18n('entity.FormAttribute.unmodifiable.label') }
                  helpBlock={ this.i18n('entity.FormAttribute.unmodifiable.help') }/>
              </Basic.AbstractForm>
            </Basic.PanelBody>
            <Basic.PanelFooter showLoading={ showLoading || _showLoading } >
              <Basic.Button type="button" level="link" onClick={ this.context.history.goBack }>
                { this.i18n('button.back') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }
                rendered={ manager.canSave(entity, _permissions) }>
                { this.i18n('button.save') }
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
        </form>
      </Basic.Div>
    );
  }
}

FormAttributeDetail.propTypes = {
  _permissions: PropTypes.arrayOf(PropTypes.string),
  isNew: PropTypes.bool,
  formDefinition: PropTypes.string,
};
FormAttributeDetail.defaultProps = {
  _permissions: null,
  isNew: false,
  formDefinition: null,
};

function select(state, component) {
  const { entityId } = component.match.params;
  const entity = manager.getEntity(state, entityId);
  if (entity) {
    entity.codeable = {
      code: entity.code,
      name: entity.name
    };
    if (entity.persistentType === PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.CODELIST)) {
      entity.codeList = entity.faceType;
    }
  }
  //
  return {
    entity,
    showLoading: manager.isShowLoading(state, null, entityId),
    _permissions: manager.getPermissions(state, null, entityId),
    supportedAttributeRenderers: DataManager.getData(state, FormAttributeManager.UI_KEY_SUPPORTED_ATTRIBUTE_RENDERERS)
  };
}

export default connect(select)(FormAttributeDetail);
