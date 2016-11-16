import React, { PropTypes } from 'react';
import classNames from 'classnames';
import merge from 'object-assign';
import Joi from 'joi';
//
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
import Icon from '../Icon/Icon';

class AbstractFormComponent extends AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.onChange = this.onChange.bind(this);
    this.isValid = this.isValid.bind(this);
    const value = this.props.value ? this.normalizeValue(this.props.value) : null;

    this.state = { value,
                  showValidationError: false, // Validation error not show on UI
                  formReadOnly: false,
                  formDisabled: false};
  }

  componentDidMount() {
    this._resolveReadOnly(this.props);
    this._resolveDisabled(this.props);
    this._resolveValidation(this.props);
  }

  componentWillReceiveProps(nextProps) {
    // Read only
    if (nextProps.readOnly !== this.props.readOnly) {
      this._resolveReadOnly(nextProps);
    }
    // Disable component
    if (nextProps.disabled !== this.props.disabled) {
      this._resolveDisabled(nextProps);
    }
    // validation
    if ((nextProps.required !== this.props.required) || (nextProps.validation !== this.props.validation)) {
      this._resolveValidation(nextProps);
    }
  }

  setState(json, cb) {
    if (json && json.value) {
      merge(json, {value: this.normalizeValue(json.value)});
    }
    super.setState(json, cb);
  }

  normalizeValue(value) {
    return value;
  }

  _resolveReadOnly(props) {
    if (props.readOnly || this.state.formReadOnly) {
      this.setState({readOnly: true});
    } else {
      this.setState({readOnly: false});
    }
  }

  _resolveDisabled(props) {
    if (props.disabled || this.state.formDisabled) {
      this.setState({disabled: true});
    } else {
      this.setState({disabled: false});
    }
  }

  _resolveValidation(props) {
    this.setState({validation: this.getValidationDefinition(props.required)}, () => {
      this.validate(false);
    });
  }

  /**
   * Focus component
   * @return {void}
   */
  focus() {
    // override in component
  }

  getRequiredValidationSchema() {
    return Joi.any().required();
  }

  getValidationDefinition(required) {
    let validation;
    if (required === true) {
      validation = this.getRequiredValidationSchema();
      if (this.props.validation) {
        validation = validation.concat(this.props.validation);
      }
    } else {
      // this is default value for not required value
      const notMandatory = Joi.any().empty('');
      validation = this.props.validation ? notMandatory.concat(this.props.validation) : notMandatory;
    }
    return validation;
  }

  isValid() {
    if (this.state.validationResult) {
      if (this.state.validationResult.isValid) {
        return true;
      }
      return false;
    }
    return true;
  }

  onChange(event) {
    if (this.props.onChange) {
      this.props.onChange(event);
    } else {
      this.setState({
        value: event.currentTarget.value
      }, () => {
        this.validate();
      });
    }
  }

/**
 * Localization validation errors
 * @param  {string} type   type of validation
 * @param  {json} params (key-value of validation params)
 * @return {string}        localized message
 */
  _localizationValidation(type, params) {
    return this.i18n('validationError.' + type, params);
  }

  validate(showValidationError) {
    const showValidations = showValidationError != null ? showValidationError : true;
    if (this.state.validation) {
      let result = this.state.validation.validate(this.state.value);
      // custom validate
      if (this.props.validate) {
        result = this.props.validate(this.state.value, result);
      }
      if (result.error) {
        let key;
        const params = {};
        if (result.error.key) {
          key = result.error.key;
        } else {
          const detail = result.error.details[0];
          key = detail.type;
          const limit = detail.context.limit;
          if (limit) {
            merge(params, {count: limit});
          }
          const valids = detail.context.valids;
          if (valids) {
            merge(params, {valids});
          }
        }
        const message = this._localizationValidation(key, params);
        this.setState({validationResult:
           {status: 'error',
            class: 'has-error has-feedback',
            isValid: false,
            message},
          showValidationError: showValidations}); // show validation error on UI
        return false;
      }
      this.setState({validationResult:
          {status: null,
             class: '',
             isValid: true,
             message: null,
             showValidationError: true},
          showValidationError: showValidations}); // show validation error on UI
      return true;
    }
  }

  getValue() {
    return this.state.value;
  }

  setValue(value) {
    this.setState({value}, this.validate.bind(this, false));
  }

  getBody() {
    return <div/>;
  }

  getValidationResult() {
    return this.state.validationResult;
  }

  render() {
    const { hidden, className, rendered } = this.props;
    if (!rendered) {
      return null;
    }
    const _className = classNames(
      className,
      'form-group',
      { hidden }
    );
    let validationClass = '';
    let feedback;

    if (this.getValidationResult()) {
      if (this.state.showValidationError) {
        validationClass = this.getValidationResult().class;
      }
      if (this.state.showValidationError && this.getValidationResult().status === 'error') {
        feedback = <Icon icon="warning-sign" className="form-control-feedback" style={{zIndex: 0}} />;
      }
    }
    return (
      <div className={_className + ' ' + validationClass} style={this.props.style}>
          {this.getBody(feedback)}
      </div>
    );
  }
}

AbstractFormComponent.propTypes = {
  // ref: PropTypes.string.isRequired, VS: ref is mandator property,
  //  but is here problem with warning after first component render
  rendered: PropTypes.bool,
  value: PropTypes.object,
  label: PropTypes.string,
  disabled: PropTypes.bool,
  hidden: PropTypes.bool,
  labelSpan: PropTypes.string, // defined span for label
  componentSpan: PropTypes.string, // defined span for component
  required: PropTypes.bool, // add default required validation and asterix
  readOnly: PropTypes.bool, // html readonly
  onChange: PropTypes.func,
  validation: PropTypes.object,
  validate: PropTypes.func, // function for custom validation (input is value and result from previous validations)
  style: PropTypes.string // form-group element style
};

AbstractFormComponent.defaultProps = {
  labelSpan: 'col-sm-3',
  componentSpan: 'col-sm-8',
  required: false,
  hidden: false,
  readOnly: false,
  disabled: false,
  rendered: true
};

export default AbstractFormComponent;
