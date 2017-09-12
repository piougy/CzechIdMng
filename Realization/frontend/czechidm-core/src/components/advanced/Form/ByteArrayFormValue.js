import TextAreaFormValue from './TextAreaFormValue';

/**
 * Byte array form value component - the same as text area for now.
 *
 * @author Radek Tomiška
 */
export default class ByteArrayFormValue extends TextAreaFormValue {

  /**
   * Fill form value field by persistent type from input value
   *
   * @param  {FormValue} formValue - form value
   * @param  {[type]} formComponent
   * @return {FormValue}
   */
  fillFormValue(formValue, rawValue) {
    formValue.byteValue = rawValue;
    return formValue;
  }

  /**
   * Returns value to ipnut from given (persisted) form value
   *
   * @param  {FormValue} formValue
   * @return {object} value by persistent type
   */
  getInputValue(formValue) {
    return formValue.byteValue;
  }

  /**
   * Returns joi validator by persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {Joi}
   */
  getInputValidation() {
    return null;
  }

}
