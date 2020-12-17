import PropTypes from 'prop-types';
import React from 'react';
import merge from 'object-assign';
import _ from 'lodash';
import equal from 'fast-deep-equal';
import classnames from 'classnames';
//
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import ApiOperationTypeEnum from '../../../enums/ApiOperationTypeEnum';
import Loading from '../Loading/Loading';

/**
 * Abstract form component
 *
 * @author Vít Švanda
 */
class AbstractForm extends AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.findFormComponentsKeys = this.findFormComponentsKeys.bind(this);
    const keys = [];
    // We need find our form components keys (only ReactElement) in form
    this.findFormComponentsKeys(this.props.children, keys, this);
    const mode = this.props.mode ? this.props.mode : ApiOperationTypeEnum.UPDATE;
    const { showLoading } = props;
    this.state = { mode, showLoading: (showLoading != null ? showLoading : true), componentsKeys: keys};
  }

  // eslint-disable-next-line camelcase
  UNSAFE_componentWillReceiveProps(nextProps) {
    if (nextProps.children !== this.props.children) {
      const {componentsKeys} = this.state;

      // We need find our form components keys and compare they with previous keys.
      // If keys (component) changed, then we set new keys and set data.
      const oldSortedComponentKeys = componentsKeys ? _.map(componentsKeys, _.clone).sort() : [];
      const newComponentKeys = [];
      this.findFormComponentsKeys(this.props.children, newComponentKeys, this);
      const newSortedComponentKeys = newComponentKeys ? _.map(newComponentKeys, _.clone).sort() : [];
      if (!(_.isEqual(oldSortedComponentKeys, newSortedComponentKeys))) {
        this.setState({componentsKeys: newComponentKeys}, () => {
          this.setData(nextProps.data);
        });
      }
    }

    if (nextProps.readOnly !== this.props.readOnly) {
      this.setReadOnly(nextProps.readOnly);
    }
    if (nextProps.disabled !== this.props.disabled) {
      this.setDisabled(nextProps.disabled);
    }
    if (nextProps.showLoading !== this.props.showLoading) {
      this.setState({showLoading: nextProps.showLoading});
    }
    if (nextProps.data !== this.props.data
      && (nextProps.data && !equal(nextProps.data, this.props.data))) {
      this.setData(nextProps.data);
    }
  }

  componentDidMount() {
    const {readOnly, data, disabled} = this.props;
    this.setReadOnly(readOnly);
    this.setDisabled(disabled);
    this.setData(data);
  }

  /**
  * Find all form-components keys and add them to componentsKey array;
  */
  findFormComponentsKeys(children, keys, basicForm) {
    React.Children.map(children, (child) => {
      if (!child) {
        return null;
      }
      // We will add only AbstractFormComponent
      if (child.type && ((child.type.prototype instanceof AbstractFormComponent) || child.type.__FormableComponent__)) {
        if (child.ref && (!child.props || !child.props.notControlled)) {
          keys.push(child.ref);
        }
        return null;
      }
      if (child.props && child.props.children && !(child.type && child.type === AbstractForm)) {
        basicForm.findFormComponentsKeys(child.props.children, keys, basicForm);
        return null;
      }
      return null;
    });
    return null;
  }

  /**
   * Find child by key
   *
   */
  findFormComponentsByKey(children, key) {
    const map = React.Children.map(children, (child) => {
      if (!child) {
        return null;
      }
      let type = child.type;
      // If component is connected to the Redux, then we need to extract our component.
      if (type && type.WrappedComponent) {
        type = type.WrappedComponent;
      }
      // We will add only AbstractFormComponent
      if (type && type.prototype instanceof AbstractFormComponent) {
        if (child.ref && (!child.props || !child.props.notControlled)) {
          if (key === child.ref) {
            return child;
          }
        }
        return null;
      }
      if (child.props && child.props.children && !(child.type && child.type === AbstractForm)) {
        return this.findFormComponentsByKey(child.props.children, key);
      }
      return null;
    });
    if (map && map.length === 1) {
      return map[0];
    }
    return null;
  }

  getFooter() {
    // some footer elements. Is override in childe (etc. BasicForm)
  }

  /**
   * Set data to all component
   * @param data
   * @param boolean setComponentStateOnly - if true, then are values sets only to state, without rendering
   */
  _setDataToComponents(data, setComponentStateOnly) {
    if (data) {
      for (const componentRef in this.state.componentsKeys) {
        if (!this.state.componentsKeys.hasOwnProperty(componentRef)) {
          continue;
        }
        const key = this.state.componentsKeys[componentRef];
        const component = this.getComponent(key);
        if (!component) {
          // component could not be rendered
          continue;
        }
        if (data.hasOwnProperty(key)) {
          const value = data[key];
          // Set new value to component
          // If component using complex value and value in given data is primitive,
          // then we try to use value from _embedded object (optimalization for prevent
          // to many request and solving problem with rights (in WF tasks ...)).
          const isPrimitive = Object(value) !== value;
          if (component.isValueComplex() === true && isPrimitive === true && data._embedded && data._embedded[key]) {
            // @todo-upgrade-10 - I need to set state directly first, because
            // AbstractForm doesn't wait on subcomponents = had old data!
            if (setComponentStateOnly) {
              component.state.value = data._embedded[key];
            } else {
              component.setValue(data._embedded[key]);
            }
          } else if (setComponentStateOnly) {
            component.state.value = value;
          } else {
            component.setValue(value);
          }
        } else if (setComponentStateOnly) {
          component.state.value = null;
        } else {
          component.setValue(null);
        }
      }
    }
  }

  // method for handle json to state
  setData(json, error, operationType) {
    if (error) {
      this.processEnded(error, operationType);
      return;
    }
    if (!this.props.rendered) {
      return;
    }
    // @todo-upgrade-10 - I had to set component's state directly and now.
    // I need to make setting of components data synchronous!
    this._setDataToComponents(json, true);

    this.setState({
      allData: merge({}, json)
    }, () => {
      this._setDataToComponents(json, false);
      this.processEnded(null, operationType);
    });
  }

  isFormValid() {
    for (const componentRef in this.state.componentsKeys) {
      if (!this.state.componentsKeys.hasOwnProperty(componentRef)) {
        continue;
      }
      if (!this.props.rendered) {
        return true;
      }
      const component = this.getComponent(this.state.componentsKeys[componentRef]);
      if (!component) {
        // component could not be rendered
        continue;
      }
      if (!component.isValid()) {
        this.showValidationError(true);
        // focus invalid component
        component.focus(true);
        return false;
      }
    }
    return true;
  }

  showValidationError(show) {
    for (const componentRef in this.state.componentsKeys) {
      if (!this.state.componentsKeys.hasOwnProperty(componentRef)) {
        continue;
      }
      const component = this.getComponent(this.state.componentsKeys[componentRef]);
      if (!component) {
        // component could not be rendered
        continue;
      }
      component.setState({showValidationError: show});
    }
  }


  /**
   * Method for compile data from all component
   * @param  mergeToAll = If is true, then will be returned whole object with
   * merged fields by components in form.
   * If is false, then will be returned only object with fields by components in form.
   */
  getData(mergeToAll = true) {
    let result = null;
    if (mergeToAll) {
      result = merge({}, this.state.allData);
    } else {
      result = {};
    }
    if (!this.props.rendered) {
      return result;
    }
    for (const componentRef in this.state.componentsKeys) {
      if (!this.state.componentsKeys.hasOwnProperty(componentRef)) {
        continue;
      }
      const key = this.state.componentsKeys[componentRef];
      const component = this.getComponent(this.state.componentsKeys[componentRef]);
      if (component) { // component cannot be rendered
        const componentValue = component.getValue();
        if (componentValue === undefined) {
          // undefined values are not sent (confidential properties etc.)
          delete result[key];
        } else {
          // merge new value
          merge(result, {
            [key]: componentValue
          });
        }
      }
    }
    return result;
  }

  /**
   * Return form component by key
   */
  getComponent(key) {
    if (this.state.componentsKeys) {
      let hasKey = false;
      for (const componentRef in this.state.componentsKeys) {
        if (!this.state.componentsKeys.hasOwnProperty(componentRef)) {
          continue;
        }
        const k = this.state.componentsKeys[componentRef];
        if (k === key) {
          hasKey = true;
        }
      }
      if (!hasKey) {
        return null;
      }
      // Work around !! ... I need get instance of react component
      // First find child for the key and then find parent componnet from
      // child and finally we can find react component for key.
      const child = this.findFormComponentsByKey(this.props.children, key);
      let component = null;
      if (child && child._owner && child._owner.stateNode && child._owner.stateNode.refs[key]) {
        component = child._owner.stateNode.refs[key];
      }
      return component;
    }
    return null;
  }

  /**
   * Operation called after asynchrone operation with form is ended.
   * @param  {object} error
   * @param  {Symbol} operationType
   * @return void
   */
  processEnded(error, operationType) {
    if (error) {
      this.addError(error);
    } else if (ApiOperationTypeEnum.UPDATE === operationType) {
      this.addMessage({title: this.i18n('message.success.update'), level: 'success'});
    } else if (ApiOperationTypeEnum.CREATE === operationType) {
      this.addMessage({title: this.i18n('message.success.create'), level: 'success'});
    } else if (ApiOperationTypeEnum.DELETE === operationType) {
      this.addMessage({title: this.i18n('message.success.delete'), level: 'success'});
    }
    this.setState({showLoading: false});
    // Form can be enabled only if props.disabled is false
    this.setDisabled(!!this.props.disabled);
  }

  processStarted() {
    this.setState({showLoading: true});
    this.setDisabled(true);
  }

  setReadOnly(readOnly) {
    this.readOnlyComponents(readOnly);
  }

  setDisabled(disabled) {
    this.disableComponents(disabled);
  }

  readOnlyComponents(readOnly) {
    for (const componentRef in this.state.componentsKeys) {
      if (!this.state.componentsKeys.hasOwnProperty(componentRef)) {
        continue;
      }
      const component = this.getComponent(this.state.componentsKeys[componentRef]);
      if (!component || !this.props.rendered) {
        return;
      }
      if (component.props.readOnly === null || component.props.readOnly === false) {
        component.setState({readOnly, formReadOnly: readOnly});
      } else {
        component.setState({formReadOnly: readOnly});
      }
    }
  }

  disableComponents(disabled) {
    for (const componentRef in this.state.componentsKeys) {
      if (!this.state.componentsKeys.hasOwnProperty(componentRef)) {
        continue;
      }
      const component = this.getComponent(this.state.componentsKeys[componentRef]);
      if (!component || !this.props.rendered) {
        return;
      }
      if (component.props.disabled === null || component.props.disabled === false) {
        component.setState({disabled, formDisabled: disabled});
      } else {
        component.setState({formDisabled: disabled});
      }
    }
  }

  render() {
    const {rendered, className, showLoading, children, style, onSubmit} = this.props;
    if (!rendered) {
      return null;
    }

    const submitForm = onSubmit ? (
      <form onSubmit={onSubmit}>
        {children}
        <input type="submit" className="hidden"/>
      </form>
    ) : children;

    return (
      <Loading showLoading={showLoading || this.state.showLoading}>
        <div className={ classnames('abstract-form', 'clearfix', className) } style={ style }>
          {submitForm}
        </div>
        {this.getFooter()}
      </Loading>
    );
  }
}

AbstractForm.propTypes = {
  showLoading: PropTypes.bool,
  readOnly: PropTypes.bool,
  disabled: PropTypes.bool,
  data: PropTypes.object,
  onSubmit: PropTypes.func
};

AbstractForm.defaultProps = {
  showLoading: false,
  readOnly: false,
  disabled: false,
  rendered: true,
  data: null
};

export default AbstractForm;
