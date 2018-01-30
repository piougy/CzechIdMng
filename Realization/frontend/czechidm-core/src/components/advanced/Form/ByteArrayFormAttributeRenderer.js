import TextAreaFormAttributeRenderer from './TextAreaFormAttributeRenderer';

/**
 * Byte array form value component - the same as text area for now.
 *
 * @author Radek Tomiška
 */
export default class ByteArraFormAttributeRenderer extends TextAreaFormAttributeRenderer {

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
    return formValue.byteValue ? formValue.byteValue : formValue.value;
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
