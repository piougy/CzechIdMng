import React from 'react';
import _ from 'lodash';
//
import * as Basic from '../../basic';
import AbstractFormAttributeRenderer from './AbstractFormAttributeRenderer';
import UuidFormAttributeRenderer from './UuidFormAttributeRenderer';
import { IdentityManager } from '../../../redux/';

const manager = new IdentityManager();

/**
 * Uuid form value component
 * - supports multiple attributes
 * - TODO: validation
 *
 * @author Radek Tomiška
 */
export default class IdentitySelectFormAttributeRenderer extends UuidFormAttributeRenderer {

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
    formValue.uuidValue = rawValue;
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
    return formValue.uuidValue ? formValue.uuidValue : formValue.value;
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
    const { attribute } = this.props;
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
      return attribute.defaultValue;
    }
    return this.getInputValue(formValue);
  }

  renderSingleInput() {
    const { attribute, readOnly, values } = this.props;
    //
    return (
      <Basic.SelectBox
        ref={ AbstractFormAttributeRenderer.INPUT }
        label={ this.getLable() }
        placeholder={ attribute.placeholder }
        manager={ manager }
        value={ !attribute.multiple ? this.toInputValue(values) : this.toInputValues(values) }
        helpBlock={ this.getHelpBlock() }
        readOnly={ readOnly || attribute.readonly }
        required={ attribute.required }
        multiSelect={ attribute.multiple }/>
    );
  }

  renderMultipleInput() {
    return this.renderSingleInput();
  }
}
