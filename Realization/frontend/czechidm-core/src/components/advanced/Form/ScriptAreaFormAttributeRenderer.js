import React from 'react';
//
import * as Basic from '../../basic';
import TextFormAttributeRenderer from './TextFormAttributeRenderer';

/**
 * Scriptarea form value component.
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
export default class ScriptAreaFormAttributeRenderer extends TextFormAttributeRenderer {

  /**
   * Returns true, when multi value mode is supported.
   *
   * @return {boolean}
   */
  supportsMultiple() {
    return false;
  }

  /**
   * Returns true, when confidential mode is supported.
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
      <Basic.ScriptArea
        ref={ TextFormAttributeRenderer.INPUT }
        mode="groovy"
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
