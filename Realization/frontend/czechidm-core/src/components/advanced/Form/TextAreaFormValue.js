import React from 'react';
//
import * as Basic from '../../basic';
import TextFormValue from './TextFormValue';

/**
 * TextArea form value component
 *
 * @author Radek Tomiška
 */
export default class TextAreaFormValue extends TextFormValue {

  /**
   * Returns true, when multi value mode is supported
   *
   * @return {boolean}
   */
  supportsMultiple() {
    return false;
  }

  renderSingleInput() {
    const { attribute, readOnly, values } = this.props;
    //
    if (attribute.confidential) {
      return super.renderSingleInput();
    }
    return (
      <Basic.TextArea
        ref={ TextFormValue.INPUT }
        label={ attribute.name}
        value={ this.toSingleInputValue(values) }
        placeholder={ attribute.placeholder }
        helpBlock={ attribute.description }
        readOnly={ readOnly || attribute.readonly }
        validation={ this.getInputValidation() }
        required={ attribute.required }/>
    );
  }

}
