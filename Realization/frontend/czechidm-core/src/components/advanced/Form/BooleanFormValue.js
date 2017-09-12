import React from 'react';
//
import * as Basic from '../../basic';
import AbstractFormValue from './AbstractFormValue';

/**
 * Boolean form value component
 *
 * @author Radek Tomiška
 */
export default class BooleanFormValue extends AbstractFormValue {

  /**
   * Fill form value field by persistent type from input value
   *
   * @param  {FormValue} formValue - form value
   * @param  {[type]} formComponent
   * @return {FormValue}
   */
  fillFormValue(formValue, rawValue) {
    formValue.booleanValue = rawValue;
    return formValue;
  }

  /**
   * Returns value to ipnut from given (persisted) form value
   *
   * @param  {FormValue} formValue
   * @return {object} value by persistent type
   */
  getInputValue(formValue) {
    return formValue.booleanValue;
  }

  renderSingleInput() {
    const { attribute, readOnly, values } = this.props;
    //
    return (
      <Basic.Checkbox
        ref={ AbstractFormValue.INPUT }
        label={ attribute.name }
        value={ this.toSingleInputValue(values) }
        helpBlock={ attribute.description }
        readOnly={ readOnly || attribute.readonly }
        required={ attribute.required }/>
    );
  }

}
