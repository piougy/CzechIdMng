import React from 'react';
import PropTypes from 'prop-types';
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

  UNSAFE_componentWillReceiveProps(nextProps) {
    if (this.refs.selectComponent) {
      this.refs.selectComponent.UNSAFE_componentWillReceiveProps(nextProps);
    }
  }

  focus() {
    if (this.refs.selectComponent) {
      this.refs.selectComponent.focus();
    }
  }

  getRequiredValidationSchema() {
    if (this.refs.selectComponent) {
      return this.refs.selectComponent.getRequiredValidationSchema();
    }
  }

  getValidationDefinition() {
    if (this.refs.selectComponent) {
      return this.refs.selectComponent.getValidationDefinition();
    }
  }

  isValid() {
    if (this.refs.selectComponent) {
      return this.refs.selectComponent.isValid();
    }
    return true;
  }

  onChange(event) {
    if (this.refs.selectComponent) {
      this.refs.selectComponent.onChange(event);
    }
  }

  validate(showValidationError, cb) {
    if (this.refs.selectComponent) {
      return this.refs.selectComponent.validate(showValidationError, cb);
    }
    return true;
  }

  getValue() {
    if (this.refs.selectComponent) {
      return this.refs.selectComponent.getValue();
    }
    return null;
  }

  setValue(value, cb) {
    if (this.refs.selectComponent) {
      this.refs.selectComponent.setValue(value, cb);
    }
  }

  setState(json, cb) {
    super.setState(json, () => {
      // FIXME: abstract form component everride standard state to show validations => we need to propage this state into component
      if (json && json.showValidationError !== undefined) {
        if (this.refs.selectComponent) {
          this.refs.selectComponent.setState({ showValidationError: json.showValidationError}, cb);
        }
      } else if (cb) {
        cb();
      }
    });
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
      let finalSearchInFields = [];
      //
      // if exists localization key for search fields, localize them
      if (component.localizationKey) {
        for (const field in component.searchInFields) {
          if (component.searchInFields.hasOwnProperty(field)) {
            finalSearchInFields.push(this.i18n(component.localizationKey + '.' + component.searchInFields[field]));
          }
        }
      } else {
        finalSearchInFields = component.searchInFields;
      }
      //
      if (finalSearchInFields) {
        finalHelpBlock.push(
          <div>
            { this.i18n('component.advanced.EntitySelectBox.defaultHelpBlock', { searchInFields: finalSearchInFields.join(', ') }) }
          </div>
        );
      }
    }
    //
    if (finalHelpBlock.length === 0) {
      return null;
    }
    return finalHelpBlock;
  }

  render() {
    const { rendered, entityType, pageSize, manager, ...others } = this.props;
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
          className="no-margin" />
      );
    }
    //
    // If component descriptor override pageSize use it!
    const pageSizeFinal = component.pageSize ? component.pageSize : pageSize;
    //
    // If component descriptor override also component use rathem them (has bigger priority)
    if (component.component) {
      const CustomEntitySelectBoxComponent = component.component;
      //
      // remove all overload attributes
      delete others.ref;
      delete others.pageSize;
      delete others.helpBlock;
      //
      return (
        <CustomEntitySelectBoxComponent
          ref="selectComponent"
          pageSize={ pageSizeFinal }
          helpBlock={ this._getHelpBlock(component) }
          { ...others } />
      );
    }
    //
    const ManagerType = component.manager;
    // Manager from in props has bigger priority, then create new instance.
    const _manager = manager || new ManagerType();
    //
    // remove all overload attributes
    delete others.ref;
    delete others.manager;
    delete others.pageSize;
    delete others.searchInFields;
    delete others.helpBlock;
    //
    return (
      <Basic.SelectBox
        ref="selectComponent"
        manager={ _manager }
        pageSize={ pageSizeFinal }
        searchInFields={ component.searchInFields }
        helpBlock={ this._getHelpBlock(component) }
        { ...others }/>
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
