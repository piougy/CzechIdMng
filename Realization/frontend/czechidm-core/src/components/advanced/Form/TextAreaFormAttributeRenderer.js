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

  renderSingleInput(originalValues) {
    const { attribute, values } = this.props;
    const showOriginalValue = originalValues ? true : false;
    //
    if (attribute.confidential) {
      return super.renderSingleInput(originalValues);
    }
    return (
      <Basic.TextArea
        ref={ TextFormAttributeRenderer.INPUT }
        label={ this.getLabel(null, showOriginalValue) }
        value={ this.toInputValue(showOriginalValue ? originalValues : values) }
        placeholder={ this.getPlaceholder() }
        helpBlock={ this.getHelpBlock() }
        readOnly={ showOriginalValue ? true : this.isReadOnly() }
        validation={ this.getInputValidation() }
        required={ this.isRequired() }/>
    );
  }

}
