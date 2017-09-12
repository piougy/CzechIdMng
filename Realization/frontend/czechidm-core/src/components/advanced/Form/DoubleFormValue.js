import Joi from 'joi';
//
import TextFormValue from './TextFormValue';

/**
 * Double form value component
 * - supports multiple and confidential attributes
 * - TODO: validation for multiple attrs
 *
 * @author Radek Tomiška
 */
export default class DoubleFormValue extends TextFormValue {

  /**
   * Returns joi validator by persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {Joi}
   */
  getInputValidation() {
    const { attribute } = this.props;
    //
    let validation = Joi.number().min(-Math.pow(10, 33)).max(Math.pow(10, 33));
    if (!attribute.required) {
      validation = validation.concat(Joi.number().allow(null));
    }
    return validation;
  }

  fillFormValue(formValue, rawValue) {
    formValue.doubleValue = rawValue;
    // TODO: validations for numbers
    return formValue;
  }

  /**
   * Returns value to ipnut from given (persisted) form value
   *
   * @param  {FormValue} formValue
   * @return {object} value by persistent type
   */
  getInputValue(formValue) {
    return formValue.doubleValue;
  }
}
