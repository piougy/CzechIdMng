import React, { PropTypes } from 'react';
//
import * as Basic from '../../basic';
import ComponentService from '../../../services/ComponentService';
//
const componentService = new ComponentService();

/**
 * Show select box with manager for entity
 *
 * @author Radek Tomiška
 * @author Ondrej Kopr
 */
export default class EntitySelectBox extends Basic.AbstractFormComponent {

  constructor(props, context) {
    super(props, context);
  }

  getValue() {
    return this.refs.selectComponent.getValue();
  }

  setValue(value) {
    this.refs.selectComponent.setValue(value);
  }

  /**
   * Returns component's help block
   */
  _getHelpBlock(component) {
    if (!component) {
      return null;
    }
    const { showDefaultHelpBlock, helpBlock } = this.props;
    const finalHelpBlock = [];
    //
    // external helpBlock has the highest priority
    if (helpBlock) {
      finalHelpBlock.push(<div>{ helpBlock }</div>);
    }
    // if show search fileds add every searchInFields to helpBlock
    if (showDefaultHelpBlock) {
      const searchInFields = component.searchInFields;
      if (searchInFields) {
        finalHelpBlock.push(<div>{ this.i18n('component.advanced.EntitySelectBox.defaultHelpBlock', { searchInFields: searchInFields.join(', ') }) }</div>);
      }
    }
    //
    return finalHelpBlock;
  }

  render() {
    const { rendered, showDefaultHelpBlock, helpBlock, entityType, ...others } = this.props;
    // standard rendered - we dont propagate rendered to underliyng component
    if (!rendered) {
      return null;
    }
    //
    // for one entity type can be found more components we want only component with highest prio
    const component = componentService.getEntitySelectBoxComponent(entityType);
    if (!component) {
      return (
        <Basic.Alert
          level="warning"
          text={ this.i18n('component.advanced.EntitySelectBox.componentNotFound', { entityType }) }
          className="no-margin"/>
      );
    }
    //
    // If component descriptor override also component use rathem them (has bigger priority)
    if (component.component) {
      const CustomEntitySelectBoxComponent = component.component;
      return (
        <CustomEntitySelectBoxComponent
          ref="selectComponent"
          helpBlock={ this._getHelpBlock(component) }
          {...others} />
        );
    }
    //
    const ManagerType = component.manager;
    const manager = new ManagerType;
    return (
      <Basic.SelectBox
        ref="selectComponent"
        manager={ manager }
        searchInFields={ component.searchInFields }
        helpBlock={ this._getHelpBlock(component) }
        {...others}/>
    );
  }
}
EntitySelectBox.propTypes = {
  ...Basic.SelectBox.propTypes,
  /**
   * Entity type (e.g. identity, role ...) for more info see README or component-descriptor
   */
  entityType: PropTypes.string.isRequired,
  /**
   * Show in help block in wich fieldswill be text search
   *
   * @type BOOLEAN
   */
  showDefaultHelpBlock: PropTypes.bool
};
EntitySelectBox.defaultProps = {
  ...Basic.SelectBox.defaultProps,
  showDefaultHelpBlock: false
};
