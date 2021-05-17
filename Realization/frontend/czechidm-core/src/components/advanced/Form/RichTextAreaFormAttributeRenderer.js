import React from 'react';
//
import RichTextArea from '../RichTextArea/RichTextArea';
import TextFormAttributeRenderer from './TextFormAttributeRenderer';

/**
 * RichTextArea form value component
 *
 * @author Radek Tomiška
 */
export default class RichTextAreaFormAttributeRenderer extends TextFormAttributeRenderer {

  /**
   * Returns true, when multi value mode is supported
   *
   * @return {boolean}
   */
  supportsMultiple() {
    return false;
  }

  /**
   * Returns true, when confidential mode is supported
   *
   * @return {boolean}
   */
  supportsConfidential() {
    return false;
  }

  renderSingleInput(originalValues) {
    const { attribute, values, validationErrors, className, style } = this.props;
    const showOriginalValue = !!originalValues;
    //
    return (
      <RichTextArea
        ref={ TextFormAttributeRenderer.INPUT }
        label={ this.getLabel(null, showOriginalValue) }
        value={ this.toInputValue(showOriginalValue ? originalValues : values) }
        placeholder={ this.getPlaceholder() }
        helpBlock={ this.getHelpBlock() }
        readOnly={ showOriginalValue ? true : this.isReadOnly() }
        required={ this.isRequired() }
        validationErrors={ validationErrors }
        validationMessage={ attribute.validationMessage }
        className={ className }
        style={ style }/>
    );
  }

}
