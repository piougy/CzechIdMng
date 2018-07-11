import Joi from 'joi';
//
import LongFormAttributeRenderer from './LongFormAttributeRenderer';

/**
 * Integer form value component
 * - supports multiple and confidential attributes
 * - TODO: validation for multiple attrs
 *
 * @author Radek Tomiška
 */
export default class IntFormAttributeRenderer extends LongFormAttributeRenderer {

  /**
   * Returns joi validator by persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {Joi}
   */
  getInputValidation() {
    let validation = Joi.number().integer().min(-2147483648).max(2147483647);
    if (!this.isRequired()) {
      validation = validation.concat(Joi.number().allow(null));
    }
    return validation;
  }
}
