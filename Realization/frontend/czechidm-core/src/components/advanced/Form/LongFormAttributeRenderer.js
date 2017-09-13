import Joi from 'joi';
//
import TextFormAttributeRenderer from './TextFormAttributeRenderer';

/**
 * Long form value component
 * - supports multiple and confidential attributes
 * - TODO: validation for multiple attrs
 *
 * @author Radek Tomiška
 */
export default class LongFormAttributeRenderer extends TextFormAttributeRenderer {

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

  /**
   * Fill form value field by persistent type from input value
   *
   * @param  {FormValue} formValue - form value
   * @param  {object} formComponent value
   * @return {FormValue}
   */
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
