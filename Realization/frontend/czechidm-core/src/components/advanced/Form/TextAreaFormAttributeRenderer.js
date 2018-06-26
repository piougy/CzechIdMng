import React from 'react';
//
import * as Basic from '../../basic';
import TextFormAttributeRenderer from './TextFormAttributeRenderer';

/**
 * TextArea form value component
 *
 * @author Radek Tomiška
 */
export default class TextAreaFormAttributeRenderer extends TextFormAttributeRenderer {

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
        ref={ TextFormAttributeRenderer.INPUT }
        label={ this.getLabel() }
        value={ this.toInputValue(values) }
        placeholder={ this.getPlaceholder() }
        helpBlock={ this.getHelpBlock() }
        readOnly={ readOnly || attribute.readonly }
        validation={ this.getInputValidation() }
        required={ attribute.required }/>
    );
  }

}
