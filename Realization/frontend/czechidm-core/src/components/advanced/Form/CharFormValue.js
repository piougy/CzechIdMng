import Joi from 'joi';
//
import TextFormValue from './TextFormValue';

/**
 * Char form value component
 * - supports multiple and confidential attributes
 *
 * @author Radek Tomiška
 */
export default class CharFormValue extends TextFormValue {

  /**
   * Returns joi validator by persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {Joi}
   */
  getInputValidation() {
    const { attribute } = this.props;
    //
    let validation = Joi.string().max(1);
    if (!attribute.required) {
      validation = validation.concat(Joi.string().allow(null).allow(''));
    }
    return validation;
  }
}
