import React from 'react';
//
import RichTextArea from '../RichTextArea/RichTextArea';
import TextFormValue from './TextFormValue';

/**
 * RichTextArea form value component
 *
 * @author Radek Tomiška
 */
export default class RichTextAreaFormValue extends TextFormValue {

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

  renderSingleInput() {
    const { attribute, readOnly, values } = this.props;
    //
    return (
      <RichTextArea
        ref={ TextFormValue.INPUT }
        label={ attribute.name}
        value={ this.toSingleInputValue(values) }
        placeholder={ attribute.placeholder }
        helpBlock={ attribute.description }
        readOnly={ readOnly || attribute.readonly }
        required={ attribute.required }/>
    );
  }

}
