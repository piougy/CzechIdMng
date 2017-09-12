import React from 'react';
//
import * as Basic from '../../basic';
import AbstractFormValue from './AbstractFormValue';

/**
 * DateTime form value component
 * - based on DateTimePicker - mode (date / datetime) is supported
 *
 * @author Radek Tomiška
 */
export default class DateTimeFormValue extends AbstractFormValue {

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
        ref={ AbstractFormValue.INPUT }
        mode={ attribute.persistentType.toLowerCase() }
        required={ attribute.required }
        label={ attribute.name }
        placeholder={ attribute.placeholder }
        value={ this.toSingleInputValue(values) }
        helpBlock={ attribute.description }
        readOnly={ readOnly || attribute.readonly }/>
    );
  }

}
