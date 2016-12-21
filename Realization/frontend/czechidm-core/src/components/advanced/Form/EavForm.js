import React, { PropTypes } from 'react';
import _ from 'lodash';
import Joi from 'joi';
//
import * as Basic from '../../basic';

/**
 * Content of eav form by given form instance (= form definition + form values)
 */
export default class EavForm extends Basic.AbstractContextComponent {

  /**
   * Return component identifier, with can be used in localization etc.
   *
   * @return {string} component identifier
   */
  getComponentKey() {
    return 'component.advanced.EavForm';
  }

  /**
   * Returns true, when form is valid, otherwise false
   *
   * @return {Boolean} [description]
   */
  isValid() {
    const { formInstance } = this.props;
    let isAllValid = true;
    formInstance.getAttributes().forEach(attribute => {
      const formComponent = this.refs[attribute.name];
      if (!formComponent) {
        // unsupported persistentType
        return true;
      }
      // we need to call validate method on all component (break is not needed)
      if (!formComponent.isValid()) {
        formComponent.setState({
          showValidationError: true
        });
        isAllValid = false;
      }
    });
    //
    return isAllValid;
  }

  /**
   * Return form definition used for form instance
   *
   * @return {formDefinition}
   */
  getFormDefinition() {
    const { formInstance } = this.props;
    //
    if (!formInstance) {
      return null;
    }
    return formInstance.getDefinition();
  }

  /**
   * Returns array of filled form values (form value object)
   *
   * @return {arrayOf(formValue)}
   */
  getValues() {
    const { formInstance } = this.props;
    const filledFormValues = [];
    //
    formInstance.getAttributes().forEach(attribute => {
      const formComponent = this.refs[attribute.name];
      if (!formComponent) {
        // unsupported persistentType
        return true;
      }
      const componentValue = formComponent.getValue();
      // undefined values are not sent (confidential properties  etc.)
      if (componentValue === undefined) {
        return true;
      }
      //
      if (attribute.multiple) {
        const formValues = formInstance.getValues(attribute.name) || [];
        // split multilines text = multi values
        if (componentValue) {
          const textValues = componentValue.split('\n');
          for (let i = 0; i < textValues.length; i++) {
            let formValue = null;
            if (i < formValues.length) {
              formValue = formValues[i];
            }
            filledFormValues.push(this._fillFormValue(formInstance, attribute, formValue, textValues[i], i));
          }
        }
      } else {
        // single value
        filledFormValues.push(this._fillFormValue(formInstance, attribute, formInstance.getSingleValue(attribute.name), componentValue));
      }
    });
    return filledFormValues;
  }

  /**
   * Fill form value by persistent type
   *
   * @param  {FormInstance} formInstance
   * @param  {FormAttribute} attribute
   * @param  {FormValue} formValue
   * @param  {[type]} formComponent
   * @return {FormValue}
   */
  _fillFormValue(formInstance, attribute, formValue, rawValue, seq = 0) {
    if (formValue === null) {
      formValue = {
        formAttribute: formInstance.getAttributeLink(attribute.name)
      };
    }
    formValue.seq = seq;
    // set value by persistent type
    switch (attribute.persistentType) {
      case 'CHAR':
      case 'TEXT':
      case 'TEXTAREA':
      case 'RICHTEXTAREA': {
        formValue.stringValue = rawValue;
        break;
      }
      case 'INT':
      case 'LONG': {
        formValue.longValue = rawValue;
        break;
      }
      case 'DOUBLE':
      case 'CURRENCY': {
        formValue.doubleValue = rawValue;
        break;
      }
      case 'BOOLEAN': {
        formValue.booleanValue = rawValue;
        break;
      }
      case 'DATE':
      case 'DATETIME': {
        formValue.dateValue = rawValue;
        break;
      }
      default: {
        this.getLogger().warn(`[EavForm]: Persistent type [${attribute.persistentType}] is not supported and not be filled and send to BE!`);
      }
    }
    return formValue;
  }

  /**
   * Return value by attribute persistent type
   *
   * @param  {FormAttrinute} attribute attribute definition
   * @param  {FormValue} formValue form value
   * @return {oneOf([string, boolean, long])}
   */
  _toInputValue(attribute, formValue) {
    if (formValue === null) {
      return attribute.defaultValue;
    }
    if (_.isArray(formValue)) {
      // multi values are transformed to multi lines
      let result = null;
      formValue.forEach(singleValue => {
        if (result !== null) {
          result += '\n';
        }
        const inputValue = this._toInputValue(attribute, singleValue);
        if (inputValue) {
          if (result === null) { // single values should not be concated
            result = inputValue;
          } else {
            result += inputValue;
          }
        }
      });
      return result;
    }
    //
    if (attribute.confidential) {
      return formValue.stringValue; // returns proxied guarded string everytime
    }
    //
    switch (attribute.persistentType) {
      case 'CHAR':
      case 'TEXT':
      case 'TEXTAREA':
      case 'RICHTEXTAREA': {
        return formValue.stringValue;
      }
      case 'INT':
      case 'LONG': {
        return formValue.longValue; // TODO: TextField warning for numbers
      }
      case 'DOUBLE':
      case 'CURRENCY': {
        return formValue.doubleValue; // TODO: TextField warning for numbers
      }
      case 'BOOLEAN': {
        return formValue.booleanValue;
      }
      case 'DATE':
      case 'DATETIME': {
        return formValue.dateValue;
      }
      default: {
        this.getLogger().warn(`[EavForm]: Persistent type [${attribute.persistentType}] is not supported and not be filled and send to BE!`);
      }
    }
    return null;
  }

  /**
   * Returns joi validator by persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {Joi}
   */
  _getInputValidation(attribute) {
    switch (attribute.persistentType) {
      case 'CHAR': {
        let validation = Joi.string().max(1);
        if (!attribute.required) {
          validation = validation.concat(Joi.string().allow(null).allow(''));
        }
        return validation;
      }
      case 'TEXT':
      case 'TEXTAREA': {
        let validation = Joi.string().max(2000);
        if (!attribute.required) {
          validation = validation.concat(Joi.string().allow(null).allow(''));
        }
        return validation;
      }
      case 'RICHTEXTAREA': {
        return null;
      }
      case 'INT': {
        let validation = Joi.number().integer().min(-2147483648).max(2147483647);
        if (!attribute.required) {
          validation = validation.concat(Joi.number().allow(null));
        }
        return validation;
      }
      case 'LONG': {
        let validation = Joi.number().integer().min(-9223372036854775808).max(9223372036854775807);
        if (!attribute.required) {
          validation = validation.concat(Joi.number().allow(null));
        }
        return validation;
      }
      case 'DOUBLE':
      case 'CURRENCY': {
        let validation = Joi.number().min(-Math.pow(10, 33)).max(Math.pow(10, 33));
        if (!attribute.required) {
          validation = validation.concat(Joi.number().allow(null));
        }
        return validation;
      }
      case 'BOOLEAN': {
        return null;
      }
      default: {
        this.getLogger().warn(`[EavForm]: Persistent type [${attribute.persistentType}] is not supported, validation will be empty!`);
      }
    }
  }


  render() {
    const { formInstance, rendered, showLoading, readOnly } = this.props;
    //
    if (!rendered || !formInstance) {
      return null;
    }
    //
    if (showLoading) {
      return (
        <Basic.Loading isStatic showLoading/>
      );
    }

    return (
      <span>
        {
          formInstance.getAttributes().map(attribute => {
            const formValues = formInstance.getValues(attribute.name);
            //
            if (attribute.multiple) {
              // unsupported variant
              if (attribute.persistentType === 'TEXTAREA'
                || attribute.persistentType === 'BOOLEAN'
                || attribute.persistentType === 'DATE'
                || attribute.persistentType === 'DATETIME'
                || attribute.persistentType === 'RICHTEXTAREA') {
                return (
                  <Basic.LabelWrapper label={attribute.displayName} >
                    <Basic.Alert level="warning" className="no-margin">
                      <div>{ this.i18n('multiple.unsupported.title', { name: attribute.persistentType }) }</div>
                      <div>{ this.i18n('multiple.unsupported.formDefinition.title') }:</div>
                      <div style={{ wordWrap: 'break-word' }}>{ this.i18n('multiple.unsupported.formDefinition.type') }: {formInstance.getDefinition().type}</div>
                      <div style={{ wordWrap: 'break-word' }}>{ this.i18n('multiple.unsupported.formDefinition.name') }: {formInstance.getDefinition().name}</div>
                    </Basic.Alert>
                  </Basic.LabelWrapper>
                );
              }
              // multi values are presented as multi lines string
              // TODO: use SelectBox component instead
              return (
                <Basic.TextArea
                  ref={attribute.name}
                  type={attribute.confidential ? 'password' : 'text'}
                  required={attribute.required}
                  label={
                    <span>
                      { attribute.displayName }
                      {' '}
                      <Basic.Tooltip placement="bottom" value={ this.i18n('multiple.title') }>{<small>({this.i18n('multiple.label')})</small>}</Basic.Tooltip>
                    </span>
                  }
                  value={this._toInputValue(attribute, formValues)}
                  helpBlock={attribute.description ? attribute.description : this.i18n('multiple.title')}
                  readOnly={readOnly || attribute.readonly}/>
              );
            }
            //
            // single field
            if (attribute.persistentType === 'TEXT'
              || attribute.persistentType === 'CHAR'
              || attribute.persistentType === 'INT'
              || attribute.persistentType === 'LONG'
              || attribute.persistentType === 'DOUBLE'
              || attribute.persistentType === 'CURRENCY') {
              return (
                <Basic.TextField
                  ref={attribute.name}
                  type={attribute.confidential ? 'password' : 'text'}
                  label={attribute.displayName}
                  value={this._toInputValue(attribute, formValues)}
                  helpBlock={attribute.description}
                  readOnly={readOnly || attribute.readonly}
                  validation={this._getInputValidation(attribute)}
                  required={attribute.required}
                  confidential={attribute.confidential}/>
              );
            } else if (attribute.confidential) {
              // other persistent type does not support confidential for now
              return (
                <Basic.LabelWrapper label={attribute.displayName}>
                  <Basic.Alert level="warning" text={ this.i18n('persistentType.unsupported.confidential', { name: attribute.persistentType}) } className="no-margin"/>
                </Basic.LabelWrapper>
              );
            }
            // date and datetime field
            if (attribute.persistentType === 'DATE'
              || attribute.persistentType === 'DATETIME') {
              return (
                <Basic.DateTimePicker
                  ref={attribute.name}
                  mode={attribute.persistentType.toLowerCase()}
                  required={attribute.required}
                  label={attribute.displayName}
                  value={this._toInputValue(attribute, formValues)}
                  helpBlock={attribute.description}
                  readOnly={readOnly || attribute.readonly}/>
              );
            }
            // textarea field
            if (attribute.persistentType === 'TEXTAREA') {
              return (
                <Basic.TextArea
                  ref={attribute.name}
                  label={attribute.displayName}
                  value={this._toInputValue(attribute, formValues)}
                  helpBlock={attribute.description}
                  readOnly={readOnly || attribute.readonly}
                  validation={this._getInputValidation(attribute)}
                  required={attribute.required}/>
              );
            }
            // richtextarea
            if (attribute.persistentType === 'RICHTEXTAREA') {
              return (
                <Basic.RichTextArea
                  ref={attribute.name}
                  label={attribute.displayName}
                  value={this._toInputValue(attribute, formValues)}
                  helpBlock={attribute.description}
                  readOnly={readOnly || attribute.readonly}
                  required={attribute.required}/>
              );
            }
            // boolean field - boolean can not be multiple
            if (attribute.persistentType === 'BOOLEAN') {
              return (
                <Basic.Checkbox
                  ref={attribute.name}
                  label={attribute.displayName}
                  value={formValues ? this._toInputValue(attribute, formValues) : (attribute.defaultValue === 'true')}
                  helpBlock={attribute.description}
                  readOnly={readOnly || attribute.readonly}
                  required={attribute.required}/>
              );
            }
            return (
              <Basic.LabelWrapper label={attribute.displayName}>
                <Basic.Alert level="warning" text={ this.i18n('persistentType.unsupported.title', { name: attribute.persistentType}) } className="no-margin"/>
              </Basic.LabelWrapper>
            );
          })
        }
      </span>
    );
  }
}

EavForm.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * FormInstance (definition + values)
   */
  formInstance: PropTypes.object,
  readOnly: PropTypes.bool
};
EavForm.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  formInstance: null,
  readOnly: false
};
