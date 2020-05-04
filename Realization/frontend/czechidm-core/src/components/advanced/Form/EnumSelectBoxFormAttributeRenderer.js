import React from 'react';
import _ from 'lodash';
//
import * as Basic from '../../basic';
import AbstractFormAttributeRenderer from './AbstractFormAttributeRenderer';
import ShortTextFormAttributeRenderer from './ShortTextFormAttributeRenderer';

/**
 * Enumeration select box component.
 *
 * @author Radek Tomiška
 */
export default class EnumSelectBoxFormAttributeRenderer extends ShortTextFormAttributeRenderer {

  /**
   * Returns true, when multi value mode is supported
   *
   * @return {boolean}
   */
  supportsMultiple() {
    return true;
  }

  /**
   * Fill form value field by persistent type from input value
   *
   * @param  {FormValue} formValue - form value
   * @param  {object} formComponent value
   * @return {FormValue}
   */
  fillFormValue(formValue, rawValue) {
    formValue.shortTextValue = rawValue;
    // common value can be used without persistent type knowlege (e.g. conversion to properties object)
    formValue.value = formValue.shortTextValue;
    //
    // TODO: validations for uuid
    return formValue;
  }

  /**
   * Returns value to ipnut from given (persisted) form value
   *
   * @param  {FormValue} formValue
   * @return {object} value by persistent type
   */
  getInputValue(formValue) {
    return formValue.shortTextValue ? formValue.shortTextValue : formValue.value;
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
    const componentValues = formComponent.getValue();
    // undefined values are not sent (confidential properties  etc.)
    if (componentValues === undefined) {
      return filledFormValues;
    }
    if (componentValues) {
      for (let i = 0; i < componentValues.length; i++) {
        let formValue = null;
        if (values && i < values.length) {
          formValue = values[i];
        }
        filledFormValues.push(this.fillFormValue(this.prepareFormValue(formValue, i), componentValues[i]));
      }
    }
    return filledFormValues;
  }

  toInputValues(formValues) {
    const { attribute, useDefaultValue } = this.props;
    //
    let formValue = null;
    if (formValues && _.isArray(formValues)) {
      if (formValues.length > 0) {
        const results = [];
        formValues.forEach(singleValue => {
          results.push(this.getInputValue(singleValue));
        });
        return results;
      }
    } else {
      formValue = formValues;
    }
    if (formValue === null) {
      if (useDefaultValue) {
        return attribute.defaultValue;
      }
      return null;
    }
    return this.getInputValue(formValue);
  }

  renderSingleInput(originalValues) {
    const { attribute, values, validationErrors, className, style, component } = this.props;
    const showOriginalValue = !!originalValues;
    //
    return (
      <Basic.EnumSelectBox
        ref={ AbstractFormAttributeRenderer.INPUT }
        label={ this.getLabel(null, showOriginalValue) }
        placeholder={ this.getPlaceholder() }
        enum={ component.enum }
        value={
          !attribute.multiple
          ?
          this.toInputValue(showOriginalValue ? originalValues : values)
          :
          this.toInputValues(showOriginalValue ? originalValues : values) }
        helpBlock={ this.getHelpBlock() }
        readOnly={ showOriginalValue ? true : this.isReadOnly() }
        required={ this.isRequired() }
        multiSelect={ attribute.multiple }
        validationErrors={ validationErrors }
        className={ className }
        style={ style}
        useSymbol={ false }/>
    );
  }

  renderMultipleInput(originalValues) {
    return this.renderSingleInput(originalValues);
  }
}
