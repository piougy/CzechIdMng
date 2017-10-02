import React from 'react';
//
import * as Basic from '../../basic';
import AbstractFormAttributeRenderer from './AbstractFormAttributeRenderer';

/**
 * DateTime form value component
 * - based on DateTimePicker - mode (date / datetime) is supported
 *
 * @author Radek Tomiška
 */
export default class DateTimeFormAttributeRenderer extends AbstractFormAttributeRenderer {

  /**
   * Fill form value field by persistent type from input value
   *
   * @param  {FormValue} formValue - form value
   * @param  {[type]} formComponent
   * @return {FormValue}
   */
  fillFormValue(formValue, rawValue) {
    formValue.dateValue = rawValue;
    return formValue;
  }

  /**
   * Returns value to ipnut from given (persisted) form value
   *
   * @param  {FormValue} formValue
   * @return {object} value by persistent type
   */
  getInputValue(formValue) {
    return formValue.dateValue;
  }

  renderSingleInput() {
    const { attribute, readOnly, values } = this.props;
    //
    return (
      <Basic.DateTimePicker
        ref={ AbstractFormAttributeRenderer.INPUT }
        mode={ attribute.persistentType.toLowerCase() }
        required={ attribute.required }
        label={ attribute.name }
        placeholder={ attribute.placeholder }
        value={ this.toInputValue(values) }
        helpBlock={ attribute.description }
        readOnly={ readOnly || attribute.readonly }/>
    );
  }

}
