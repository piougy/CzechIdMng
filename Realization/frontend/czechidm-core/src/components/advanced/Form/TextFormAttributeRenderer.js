import React from 'react';
import _ from 'lodash';
import Joi from 'joi';
//
import * as Basic from '../../basic';
import AbstractFormAttributeRenderer from './AbstractFormAttributeRenderer';

/**
 * Text form value component
 * - supports multiple and confidential attributes
 *
 * @author Radek Tomiška
 */
export default class TextFormAttributeRenderer extends AbstractFormAttributeRenderer {

  /**
   * Returns true, when confidential mode is supported
   *
   * @return {boolean}
   */
  supportsConfidential() {
    return true;
  }

  /**
   * Returns true, when multi value mode is supported
   *
   * @return {boolean}
   */
  supportsMultiple() {
    return true;
  }

  /**
   * Form values from multi input
   *
   * @return {arrayOf(FormValue)}
   */
  toFormValues() {
    const { values } = this.props;
    const formComponent = this.refs[AbstractFormAttributeRenderer.INPUT];
    const filledFormValues = [];
    //
    if (!formComponent) {
      // not supported compoenents
      return filledFormValues;
    }
    const componentValue = formComponent.getValue();
    // undefined values are not sent (confidential properties  etc.)
    if (componentValue === undefined) {
      return filledFormValues;
    }
    if (componentValue) {
      const textValues = componentValue.split('\n');
      for (let i = 0; i < textValues.length; i++) {
        let formValue = null;
        if (values && i < values.length) {
          formValue = values[i];
        }
        filledFormValues.push(this.fillFormValue(this.prepareFormValue(formValue, i), textValues[i]));
      }
    }
    return filledFormValues;
  }

  /**
   * Fill form value field by persistent type from input value
   *
   * @param  {FormValue} formValue - form value
   * @param  {[type]} formComponent
   * @return {FormValue}
   */
  fillFormValue(formValue, rawValue) {
    formValue.stringValue = rawValue;
    return formValue;
  }

  /**
   * Returns value to ipnut from given (persisted) form value
   *
   * @param  {FormValue} formValue
   * @return {object} value by persistent type
   */
  getInputValue(formValue) {
    return formValue.stringValue;
  }

  /**
   * Return value by attribute persistent type
   *
   * @param  {FormValue} formValue form value
   * @return {oneOf([string, boolean, long])}
   */
  toInputValues(formValues) {
    const { attribute } = this.props;
    //
    if (formValues === null) {
      return attribute.defaultValue;
    }
    if (_.isArray(formValues)) {
      // multi values are transformed to multi lines
      let result = null;
      formValues.forEach(singleValue => {
        if (result !== null) {
          if (!attribute.multiple) {
            return false;
          }
          result += '\n';
        }
        const inputValue = this.toInputValues(singleValue);
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
      return formValues.stringValue; // returns proxied guarded string everytime
    }
    return this.getInputValue(formValues);
  }

  /**
   * Returns joi validator by persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {Joi}
   */
  getInputValidation() {
    const { attribute } = this.props;
    //
    let validation = Joi.string().max(2000);
    if (!attribute.required) {
      validation = validation.concat(Joi.string().allow(null).allow(''));
    }
    return validation;
  }

  renderSingleInput() {
    const { attribute, readOnly, values } = this.props;
    //
    return (
      <Basic.TextField
        ref={ AbstractFormAttributeRenderer.INPUT }
        type={ attribute.confidential ? 'password' : 'text' }
        label={ attribute.name }
        placeholder={ attribute.placeholder }
        value={ this.toInputValue(values) }
        helpBlock={ attribute.description }
        readOnly={ readOnly || attribute.readonly }
        validation={ this.getInputValidation() }
        required={ attribute.required }
        confidential={ attribute.confidential }/>
    );
  }

  renderMultipleInput() {
    const { attribute, readOnly, values } = this.props;
    //
    return (
      <Basic.TextArea
        ref={ AbstractFormAttributeRenderer.INPUT }
        type={ attribute.confidential ? 'password' : 'text' }
        required={ attribute.required }
        label={
          <span>
            { attribute.name }
            {' '}
            <Basic.Tooltip placement="bottom" value={ this.i18n('component.advanced.EavForm.multiple.title') }>
              {
                <small>({this.i18n('component.advanced.EavForm.multiple.label')})</small>
              }
            </Basic.Tooltip>
          </span>
        }
        value={ this.toInputValues(values) }
        helpBlock={ attribute.description ? attribute.description : this.i18n('multiple.title') }
        readOnly={ readOnly || attribute.readonly }
        placeholder={ attribute.placeholder }/>
    );
  }
}
