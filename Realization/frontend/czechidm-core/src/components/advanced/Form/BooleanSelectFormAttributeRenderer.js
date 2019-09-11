import React from 'react';
//
import * as Basic from '../../basic';
import AbstractFormAttributeRenderer from './AbstractFormAttributeRenderer';
import BooleanFormAttributeRenderer from './BooleanFormAttributeRenderer';

/**
 * Boolean selectbox form value component
 *
 * @author Radek Tomiška
 */
export default class BooleanSelectFormAttributeRenderer extends BooleanFormAttributeRenderer {

  /**
   * Fill form value field by persistent type from input value
   *
   * @param  {FormValue} formValue - form value
   * @param  {[type]} formComponent
   * @return {FormValue}
   */
  fillFormValue(formValue, rawValue) {
    formValue.booleanValue = rawValue;
    // common value can be used without persistent type knowlege (e.g. conversion to properties object)
    formValue.value = formValue.booleanValue;
    //
    return formValue;
  }

  /**
   * Returns value to ipnut from given (persisted) form value
   *
   * @param  {FormValue} formValue
   * @return {object} value by persistent type
   */
  getInputValue(formValue) {
    const value = formValue.booleanValue ? formValue.booleanValue : formValue.value;
    if (value === null || value === undefined) {
      return null;
    }
    // Warning: string representation is needed (false value not work as selected value for react-select clearable functionality)
    return value.toString();
  }

  renderSingleInput(originalValues) {
    const { values, validationErrors, className, style } = this.props;
    const showOriginalValue = !!originalValues;
    //
    return (
      <Basic.BooleanSelectBox
        ref={ AbstractFormAttributeRenderer.INPUT }
        label={ this.getLabel(null, showOriginalValue) }
        value={ this.toInputValue(showOriginalValue ? originalValues : values) }
        helpBlock={ this.getHelpBlock() }
        readOnly={ showOriginalValue ? true : this.isReadOnly() }
        required={ this.isRequired() }
        placeholder={ this.getPlaceholder() }
        validationErrors={ validationErrors }
        className={ className }
        style={ style}/>
    );
  }

}
