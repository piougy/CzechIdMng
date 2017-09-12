import Joi from 'joi';
//
import LongFormValue from './LongFormValue';

/**
 * Integer form value component
 * - supports multiple and confidential attributes
 * - TODO: validation for multiple attrs
 *
 * @author Radek Tomiška
 */
export default class IntFormValue extends LongFormValue {

  /**
   * Returns joi validator by persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {Joi}
   */
  getInputValidation() {
    const { attribute } = this.props;
    //
    let validation = Joi.number().integer().min(-2147483648).max(2147483647);
    if (!attribute.required) {
      validation = validation.concat(Joi.number().allow(null));
    }
    return validation;
  }
}
