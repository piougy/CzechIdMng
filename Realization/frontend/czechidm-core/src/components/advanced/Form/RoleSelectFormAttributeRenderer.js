import React from 'react';
//
import SearchParameters from '../../../domain/SearchParameters';
import AbstractFormAttributeRenderer from './AbstractFormAttributeRenderer';
import SelectBoxFormAttributeRenderer from './SelectBoxFormAttributeRenderer';
import RoleSelect from '../RoleSelect/RoleSelect';

/**
 * Role select component
 * - TODO: validation
 *
 * @author Radek Tomiška
 */
export default class RoleSelectFormAttributeRenderer extends SelectBoxFormAttributeRenderer {

  constructor(props) {
    super(props);
    this.state = {
      ...this.state
    };
  }

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

  renderSingleInput(originalValues) {
    const { attribute, values, uiKey, validationErrors, className, style, component } = this.props;
    const showOriginalValue = originalValues ? true : false;
    //
    // set search name into force search parameters
    let forceSearchParameters = null;
    if (component.searchName) {
      forceSearchParameters = new SearchParameters('can-be-requested');
    }
    //
    return (
      <RoleSelect
        ref={ AbstractFormAttributeRenderer.INPUT }
        uiKey={ uiKey || `form-attribute-${attribute.code}` }
        manager={ this.getManager() }
        forceSearchParameters={ forceSearchParameters }
        header={ this.getLabel(null, showOriginalValue) }
        label={ this.getLabel(null, showOriginalValue) }
        placeholder={ this.getPlaceholder() }
        helpBlock={ this.getHelpBlock() }
        value={ !attribute.multiple ? this.toInputValue(showOriginalValue ? originalValues : values) : this.toInputValues(showOriginalValue ? originalValues : values) }
        readOnly={ showOriginalValue ? true : this.isReadOnly() }
        required={ this.isRequired() }
        multiSelect={ attribute.multiple }
        validationErrors={ validationErrors }
        className={ className }
        style={ style}/>
    );
  }

  renderMultipleInput(originalValues) {
    return this.renderSingleInput(originalValues);
  }
}
