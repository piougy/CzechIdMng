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

  /**
   * Returns entity info component by given type. Geto component cann't be static
   *
   * @return {object} component or null
   */
  getComponent() {
    const { showSearchFields, helpBlock, entityType, ...others } = this.props;
    // entityType must exists!
    if (!entityType) {
      return null;
    }
    //
    const finalProps = others;
    //
    // for one entity type can be found more components we want only component with highest prio
    let finalComponent = null;
    //
    componentService.getComponentDefinitions(ComponentService.ENTITY_SELECT_BOX_COMPONENT_TYPE).find(component => {
      if (!component.entityType) {
        return;
      }
      // entity type must be single value, every select box has only one type of entities, now :-)
      if (component.entityType.toLowerCase() === entityType.toLowerCase()) {
        if (finalComponent == null) {
          finalComponent = component;
        } else if (finalComponent.priority < component.priority) {
          finalComponent = component;
        }
      }
    });
    //
    // If component descriptor override also component use rathem them (has bigger priority)
    if (finalComponent.component) {
      const AnotherComponent = finalComponent.component;
      return <AnotherComponent ref="selectComponent" {...others} />;
    }
    //
    // if show search fileds add every searchInFields to helpBlock
    if (showSearchFields) {
      const searchInFields = finalComponent.searchInFields;
      let finalHelpBlock;
      if (helpBlock) {
        finalHelpBlock = helpBlock + this.i18n('component.advanced.EntitySelectBox.defaultHelpBlock', { searchInFields: searchInFields.join(', ') });
      } else {
        finalHelpBlock = this.i18n('component.advanced.EntitySelectBox.defaultHelpBlock', { searchInFields: searchInFields.join(', ') });
      }
      //
      finalProps.helpBlock = finalHelpBlock;
    }
    //
    const manager = new finalComponent.Manager();
    return (
      <Basic.SelectBox
        {...finalProps}
        ref="selectComponent"
        manager={manager}
        searchInFields={finalComponent.searchInFields}/>
    );
  }

  getValue() {
    return this.refs.selectComponent.getValue();
  }

  setValue(value) {
    this.refs.selectComponent.setValue(value);
  }

  render() {
    const { rendered } = this.props;
    // standard rendered - we dont propagate rendered to underliyng component
    if (!rendered) {
      return null;
    }
    return this.getComponent();
  }
}
EntitySelectBox.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * Entity type (e.g. identity, role ...) for more info see README or component-descriptor
   */
  entityType: PropTypes.string.isRequired,
  /**
   * Show in help block in wich fieldswill be text search
   *
   * @type BOOLEAN
   */
  showSearchFields: PropTypes.bool
};
EntitySelectBox.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  showSearchFields: false
};
