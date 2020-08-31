import React from 'react';
//
import SearchParameters from '../../../domain/SearchParameters';
import AbstractFormAttributeRenderer from './AbstractFormAttributeRenderer';
import SelectBoxFormAttributeRenderer from './SelectBoxFormAttributeRenderer';
import IdentitySelect from '../IdentitySelect/IdentitySelect';

/**
 * Identity select component.
 * - TODO: validation
 *
 * @author Radek Tomiška
 * @since 10.1.0
 */
export default class IdentitySelectFormAttributeRenderer extends SelectBoxFormAttributeRenderer {

  /**
   * Returns true, when multi value mode is supported
   *
   * @return {boolean}
   */
  supportsMultiple() {
    return true;
  }

  /**
   * Returns true, when confidential mode is supported
   *
   * @return {boolean}
   */
  supportsConfidential() {
    return false;
  }

  /**
   * Cannot select disabled identity.
   *
   * @return {Boolean} true by deafult => disabled identity cannot be selected
   * @since 10.5.0
   */
  isDisableable() {
    return true;
  }

  renderSingleInput(originalValues) {
    const { attribute, values, uiKey, validationErrors, className, style, component } = this.props;
    const showOriginalValue = !!originalValues;
    //
    // set search name into force search parameters
    let forceSearchParameters = null;
    if (component && component.searchName) {
      forceSearchParameters = new SearchParameters(component.searchName);
    }
    //
    return (
      <IdentitySelect
        ref={ AbstractFormAttributeRenderer.INPUT }
        uiKey={ uiKey || `form-attribute-${attribute.code}` }
        manager={ this.getManager() }
        forceSearchParameters={ forceSearchParameters }
        header={ this.getLabel(null, showOriginalValue) }
        label={ this.getLabel(null, showOriginalValue) }
        placeholder={ this.getPlaceholder() }
        helpBlock={ this.getHelpBlock() }
        value={
          !attribute.multiple
          ?
          this.toInputValue(showOriginalValue ? originalValues : values)
          :
          this.toInputValues(showOriginalValue ? originalValues : values)
        }
        readOnly={ showOriginalValue ? true : this.isReadOnly() }
        required={ this.isRequired() }
        multiSelect={ attribute.multiple }
        validationErrors={ validationErrors }
        className={ className }
        style={ style }
        disableable={ this.isDisableable() }/>
    );
  }

  renderMultipleInput(originalValues) {
    return this.renderSingleInput(originalValues);
  }
}
