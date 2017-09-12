import Joi from 'joi';
//
import TextFormValue from './TextFormValue';

/**
 * Long form value component
 * - supports multiple and confidential attributes
 * - TODO: validation for multiple attrs
 *
 * @author Radek Tomiška
 */
export default class LongFormValue extends TextFormValue {

  /**
   * Returns joi validator by persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {Joi}
   */
  getInputValidation() {
    const { attribute } = this.props;
    //
    let validation = Joi.number().integer().min(-9223372036854775808).max(9223372036854775807);
    if (!attribute.required) {
      validation = validation.concat(Joi.number().allow(null));
    }
    return validation;
  }

  fillFormValue(formValue, rawValue) {
    formValue.longValue = rawValue;
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
    return formValue.longValue;
  }
}
