import React, { PropTypes } from 'react';
import _ from 'lodash';
//
import * as Basic from '../../../components/basic';
import * as Utils from '../../../utils';
import { AutomaticRoleAttributeRuleManager, SecurityManager, FormAttributeManager } from '../../../redux';
import AutomaticRoleAttributeRuleTypeEnum from '../../../enums/AutomaticRoleAttributeRuleTypeEnum';
import AutomaticRoleAttributeRuleComparisonEnum from '../../../enums/AutomaticRoleAttributeRuleComparisonEnum';
import ContractAttributeEnum from '../../../enums/ContractAttributeEnum';
import IdentityAttributeEnum from '../../../enums/IdentityAttributeEnum';
/**
 * Constant for get eav attribute for identity contract
 * @type {String}
 */
const CONTRACT_EAV_TYPE = 'eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract';
/**
 * Constatn for get eav attribute for identity
 * @type {String}
 */
const IDENTITY_EAV_TYPE = 'eu.bcvsolutions.idm.core.model.entity.IdmIdentity';

const DEFINITION_TYPE_FILTER = 'definitionType';
/**
 * Detail rules of automatic role attribute
 *
 */
export default class AutomaticRoleAttributeRuleDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new AutomaticRoleAttributeRuleManager();
    this.formAttributeManager = new FormAttributeManager();
    this.state = {
      showLoading: false,
      typeForceSearchParameters: null, // force search parameters for EAV attribute
      type: AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.IDENTITY), // default type, show when create new entity
      valueRequired: true, // flag for required field
      formAttribute: null, // instance of form attribute, is used for computed field input
      attributeName: null // name of identity attribute
    };
  }

  getContentKey() {
    return 'content.automaticRoles.attribute';
  }

  componentDidMount() {
    const { entity } = this.props;
    this._initForm(entity);
  }

  /**
   * Method check if props in this component is'nt different from new props.
   */
  componentWillReceiveProps(nextProps) {
    // check id of old and new entity
    if (nextProps.entity.id !== this.props.entity.id || nextProps.entity.attributeName !== this.props.entity.attributeName) {
      this._initForm(nextProps.entity);
    }
  }

  /**
   * Method for basic initial form
   */
  _initForm(entity) {
    let attributeName = null;
    if (entity !== undefined) {
      let formAttribute = null;
      if (!entity.id) {
        entity.type = AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.IDENTITY);
        entity.comparison = AutomaticRoleAttributeRuleComparisonEnum.findKeyBySymbol(AutomaticRoleAttributeRuleComparisonEnum.EQUALS);
        entity.attributeName = IdentityAttributeEnum.USERNAME;
        attributeName = IdentityAttributeEnum.USERNAME;
      } else {
        if (entity.attributeName) {
          if (entity.type === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.IDENTITY)) {
            entity.attributeName = IdentityAttributeEnum.getEnum(entity.attributeName);
            attributeName = IdentityAttributeEnum.findKeyBySymbol(entity.attributeName);
          } else {
            entity.attributeName = ContractAttributeEnum.getEnum(entity.attributeName);
            attributeName = ContractAttributeEnum.findKeyBySymbol(entity.attributeName);
          }
        } else {
          // eav is used
          if (entity._embedded && entity._embedded.formAttribute) {
            formAttribute = entity._embedded.formAttribute;
          }
        }
      }
      this.setState({
        typeForceSearchParameters: this._getForceSearchParametersForType(entity.type),
        type: entity.type,
        formAttribute,
        attributeName
      });
      this.refs.type.focus();
      this.refs.form.setData(entity);
    }
  }

  _getForceSearchParametersForType(type) {
    let typeForceSearchParameters = this.formAttributeManager.getDefaultSearchParameters();
    if (type === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.IDENTITY_EAV)) {
      typeForceSearchParameters = typeForceSearchParameters.setFilter(DEFINITION_TYPE_FILTER, IDENTITY_EAV_TYPE);
    } else if (type === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.CONTRACT_EAV)) {
      typeForceSearchParameters = typeForceSearchParameters.setFilter(DEFINITION_TYPE_FILTER, CONTRACT_EAV_TYPE);
    } else {
      typeForceSearchParameters = null;
    }
    return typeForceSearchParameters;
  }

  /**
   * Default save method that catch save event from form.
   */
  save(afterAction = 'CLOSE', event) {
    const { uiKey, attributeId } = this.props;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.value.isValid()) {
      // with eav component doesn't  work validate by form
      return;
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    // modal window with information about recalculate automatic roles
    this.refs['recalculate-automatic-role'].show(
      this.i18n(`content.automaticRoles.recalculate.message`),
      this.i18n(`content.automaticRoles.recalculate.header`)
    ).then(() => {
      this._saveInternal(true, attributeId, uiKey, afterAction);
    }, () => {
      this._saveInternal(false, attributeId, uiKey, afterAction);
    });
  }

  _saveInternal(recalculatedRoles, attributeId, uiKey, afterAction) {
    const { entity } = this.props;
    this.setState({
      showLoading: true
    }, this.refs.form.processStarted());

    const formData = this.refs.form.getData();
    let value = this.refs.value.getValue();
    if (_.isObject(value)) {
      // eav form value
      value = this._getValueFromEav(value);
    }
    // attribute in backend is String type, we must explicit cast to string
    formData.value = String(value);
    //
    formData.automaticRoleAttribute = attributeId;
    // we must transform attribute name with case sensitive letters
    if (formData.attributeName) {
      if (formData.type === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.IDENTITY)) {
        formData.attributeName = IdentityAttributeEnum.getField(IdentityAttributeEnum.findKeyBySymbol(formData.attributeName));
      } else {
        formData.attributeName = ContractAttributeEnum.getField(ContractAttributeEnum.findKeyBySymbol(formData.attributeName));
      }
    }
    //
    const newSavedEntity = _.merge(entity, formData);
    if (newSavedEntity.id === undefined) {
      if (recalculatedRoles) {
        this.context.store.dispatch(this.manager.createAndRecalculateEntity(newSavedEntity, `${uiKey}-detail`, (createdEntity, error) => {
          this._afterSave(createdEntity, error, afterAction);
        }));
      } else {
        this.context.store.dispatch(this.manager.createEntity(newSavedEntity, `${uiKey}-detail`, (createdEntity, error) => {
          this._afterSave(createdEntity, error, afterAction);
        }));
      }
    } else {
      if (recalculatedRoles) {
        this.context.store.dispatch(this.manager.updateAndRecalculateEntity(newSavedEntity, `${uiKey}-detail`, (updatedEntity, error) => {
          this._afterSave(updatedEntity, error, afterAction);
        }));
      } else {
        this.context.store.dispatch(this.manager.updateEntity(newSavedEntity, `${uiKey}-detail`, (updatedEntity, error) => {
          this._afterSave(updatedEntity, error, afterAction);
        }));
      }
    }
  }

  /**
   * Get field form EAV, saved eav form value contains
   * value, seq and then value by persistent type for example: dateValue, doubleValue, ...
   */
  _getValueFromEav(eav) {
    for (const field in eav) {
      if (field !== 'value' && field.includes('Value')) {
        return eav[field];
      }
    }
  }

  /**
   * Method set showLoading to false and if is'nt error then show success message
   */
  _afterSave(entity, error, afterAction) {
    if (error) {
      this.setState({
        showLoading: false
      }, this.refs.form.processEnded());
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    if (afterAction !== 'CONTINUE') {
      this.context.router.goBack();
    } else {
      this.setState({
        showLoading: false
      }, this.refs.form.processEnded());
      //
      this.context.router.replace('/automatic-role/attributes/' + entity.automaticRoleAttribute + '/rule/' + entity.id);
    }
  }

  _typeChange(option) {
    let typeForceSearchParameters = null;
    if (option) {
      typeForceSearchParameters = this._getForceSearchParametersForType(option.value);
    }
    //
    // clear values in specific fields
    this.refs.attributeName.setValue(null);
    this.refs.formAttribute.setValue(null);
    //
    this.setState({
      typeForceSearchParameters,
      type: option.value
    });
  }

  _comparsionChange(option) {
    let valueRequired = false;
    if (option.value === AutomaticRoleAttributeRuleComparisonEnum.findKeyBySymbol(AutomaticRoleAttributeRuleComparisonEnum.EQUALS)) {
      valueRequired = true;
    }
    //
    this.setState({
      valueRequired
    });
  }

  _formAttributeChange(option) {
    // just change formAttribute
    this.setState({
      formAttribute: option
    });
  }

  _attributeNameChange(option) {
    // set new attribute name
    this.setState({
      attributeName: option.value
    });
  }

  /**
   * Return component that corespond with persisntent type of value type.
   * As default show text field.
   */
  _getValueField(type, valueRequired, formAttribute, attributeName) {
    const { entity } = this.props;
    const value = entity.value;
    let finalComponent = null;
    //
    if (type === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.IDENTITY) ||
    type === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.CONTRACT)) {
      finalComponent = this._getValueFieldForEntity(entity, type, value, attributeName);
    } else if (formAttribute) {
      finalComponent = this._getValueFieldForEav(formAttribute, value, valueRequired);
    } else {
      // form attribute doesn't exists
      finalComponent = this._getDefaultTextField(value);
    }
    return finalComponent;
  }

  _getValueFieldForEntity(entity, type, value, attributeName) {
    if (attributeName == null) {
      return this._getDefaultTextField(value);
    }
    // identity attributes
    if (type === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.IDENTITY)) {
      // disabled is obly attribute that has different face
      if (IdentityAttributeEnum.findSymbolByKey(attributeName) === IdentityAttributeEnum.DISABLED) {
        return this._getDefaultBooleanSelectBox(value);
      }
      return this._getDefaultTextField(value);
    }
    // contracts attributes
    // contract has externe and main as boolean and valid attributes as date
    if (ContractAttributeEnum.findSymbolByKey(attributeName) === ContractAttributeEnum.MAIN || ContractAttributeEnum.findSymbolByKey(attributeName) === ContractAttributeEnum.EXTERNE) {
      return this._getDefaultBooleanSelectBox(value);
    } else if (ContractAttributeEnum.findSymbolByKey(attributeName) === ContractAttributeEnum.VALID_FROM || ContractAttributeEnum.findSymbolByKey(attributeName) === ContractAttributeEnum.VALID_TILL) {
      return this._getDefaultDateTimePicker(value);
    }
    return this._getDefaultTextField(value);
  }

  _getValueFieldForEav(formAttribute, value, valueRequired) {
    const component = this.formAttributeManager.getFormComponent(formAttribute);
    if (!component || !component.component) {
      // when component doesn't exists show default field
      return this._getDefaultTextField(value);
    }
    const FormValueComponent = component.component;
    //
    // override helpBlock, label and placeholder
    formAttribute.description = this.i18n('entity.AutomaticRole.attribute.value.help');
    formAttribute.name = this.i18n('entity.AutomaticRole.attribute.value.label');
    formAttribute.placeholder = '';
    formAttribute.defaultValue = null;
    formAttribute.required = valueRequired;
    //
    // is neccessary transform value to array
    return (
      <FormValueComponent
        ref="value"
        required={valueRequired}
        attribute={formAttribute}
        values={[{value}]}/>
    );
  }

  /**
   * Return simple text field for value input
   */
  _getDefaultTextField(value) {
    const { valueRequired } = this.state;
    return (
      <Basic.TextField
        ref="value"
        value={value}
        required={valueRequired}
        label={this.i18n('entity.AutomaticRole.attribute.value.label')}
        helpBlock={this.i18n('entity.AutomaticRole.attribute.value.help')}/>);
  }

  /**
   * Return date time picker
   */
  _getDefaultDateTimePicker(value) {
    const { valueRequired } = this.state;
    return (<Basic.DateTimePicker
      ref="value"
      mode="date"
      value={value}
      required={valueRequired}
      label={this.i18n('entity.AutomaticRole.attribute.value.label')}
      helpBlock={this.i18n('entity.AutomaticRole.attribute.value.help')}/>);
  }

  /**
   * Return default boolean select box
   */
  _getDefaultBooleanSelectBox(value) {
    const { valueRequired } = this.state;
    return (
      <Basic.BooleanSelectBox
        ref="value"
        value={value}
        required={valueRequired}
        label={this.i18n('entity.AutomaticRole.attribute.value.label')}
        helpBlock={this.i18n('entity.AutomaticRole.attribute.value.help')}/>);
  }

  render() {
    const { uiKey, entity } = this.props;
    const {
      showLoading,
      typeForceSearchParameters,
      type,
      valueRequired,
      formAttribute,
      attributeName
    } = this.state;
    //
    return (
      <div>
        <Basic.Confirm ref="recalculate-automatic-role" level="success"/>
        <form onSubmit={this.save.bind(this, 'CONTINUE')}>
          <Basic.AbstractForm
            ref="form"
            uiKey={uiKey}
            readOnly={!SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'AUTOMATICROLERULE_CREATE' : 'AUTOMATICROLERULE_UPDATE')}
            style={{ padding: '15px 15px 0 15px' }}>
            <Basic.EnumSelectBox
              ref="type"
              required
              label={this.i18n('entity.AutomaticRole.attribute.type.label')}
              helpBlock={this.i18n('entity.AutomaticRole.attribute.type.help')}
              enum={AutomaticRoleAttributeRuleTypeEnum}
              onChange={this._typeChange.bind(this)}/>
            <Basic.EnumSelectBox
              ref="attributeName"
              clearable={false}
              label={this.i18n('entity.AutomaticRole.attribute.attributeName')}
              enum={type === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.IDENTITY) ? IdentityAttributeEnum : ContractAttributeEnum}
              hidden={typeForceSearchParameters !== null}
              onChange={this._attributeNameChange.bind(this)}
              required={!(typeForceSearchParameters !== null)}/>
            <Basic.SelectBox
              ref="formAttribute"
              useFirst
              clearable={false}
              onChange={this._formAttributeChange.bind(this)}
              forceSearchParameters={typeForceSearchParameters}
              label={this.i18n('entity.AutomaticRole.attribute.formAttribute')}
              hidden={typeForceSearchParameters === null}
              required={!(typeForceSearchParameters === null)}
              manager={this.formAttributeManager}/>
            <Basic.Row>
              <div className="col-lg-4">
                <Basic.EnumSelectBox
                  ref="comparison"
                  required
                  useFirst
                  onChange={this._comparsionChange.bind(this)}
                  label={this.i18n('entity.AutomaticRole.attribute.comparison')}
                  enum={AutomaticRoleAttributeRuleComparisonEnum}/>
              </div>
              <div className="col-lg-8">
                { this._getValueField(type, valueRequired, formAttribute, attributeName) }
              </div>
            </Basic.Row>
          </Basic.AbstractForm>

          <Basic.PanelFooter showLoading={showLoading} >
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
            <Basic.SplitButton
              level="success"
              title={ this.i18n('button.saveAndClose') }
              onClick={ this.save.bind(this, 'CLOSE') }
              showLoading={ showLoading }
              showLoadingIcon
              showLoadingText={ this.i18n('button.saving') }
              rendered={SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'AUTOMATICROLERULE_CREATE' : 'AUTOMATICROLERULE_UPDATE')}
              dropup>
              <Basic.MenuItem
                eventKey="1"
                onClick={this.save.bind(this, 'CONTINUE')}>
                {this.i18n('button.saveAndContinue')}
              </Basic.MenuItem>
            </Basic.SplitButton>
          </Basic.PanelFooter>
          {/* onEnter action - is needed because SplitButton is used instead standard submit button */}
          <input type="submit" className="hidden"/>
        </form>
      </div>
    );
  }
}

AutomaticRoleAttributeRuleDetail.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
  attributeId: PropTypes.string
};
AutomaticRoleAttributeRuleDetail.defaultProps = {
};
