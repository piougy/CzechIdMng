import React from 'react';
//
import SearchParameters from '../../../domain/SearchParameters';
import AbstractFormAttributeRenderer from './AbstractFormAttributeRenderer';
import SelectBoxFormAttributeRenderer from './SelectBoxFormAttributeRenderer';
import TreeNodeSelect from '../TreeNodeSelect/TreeNodeSelect';

/**
 * Tree select component
 * - TODO: validation
 *
 * @author Radek Tomiška
 */
export default class TreeNodeSelectFormAttributeRenderer extends SelectBoxFormAttributeRenderer {

  /**
   * Returns true, when confidential mode is supported
   *
   * @return {boolean}
   */
  supportsConfidential() {
    return false;
  }

  renderSingleInput(originalValues) {
    const { attribute, values, uiKey, validationErrors, className, style } = this.props;
    const showOriginalValue = !!originalValues;
    //
    let forceSearchParameters = null;
    if (attribute && attribute.properties && attribute.properties['tree-type']) {
      // tree node from given tree type only
      forceSearchParameters = new SearchParameters().setFilter('treeTypeId', attribute.properties['tree-type']);
    }
    //
    return (
      <TreeNodeSelect
        ref={ AbstractFormAttributeRenderer.INPUT }
        uiKey={ uiKey || `form-attribute-${attribute.code}` }
        manager={ this.getManager() }
        header={ this.getLabel(null, showOriginalValue) }
        label={ this.getLabel(null, showOriginalValue) }
        placeholder={ this.getPlaceholder() }
        helpBlock={ this.getHelpBlock() }
        showTreeType={ false }
        forceSearchParameters={ forceSearchParameters }
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
        validationMessage={ attribute.validationMessage }
        className={ className }
        style={ style}/>
    );
  }
}
