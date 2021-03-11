import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import merge from 'object-assign';
import Joi from 'joi';
//
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
import Icon from '../Icon/Icon';
import HelpIcon from '../HelpIcon/HelpIcon';

/**
 * @author Vít Švanda
 * @author Ondrej Husnik
 */
class AbstractFormComponent extends AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.onChange = this.onChange.bind(this);
    this.isValid = this.isValid.bind(this);
    this.state = {
      value: null,
      showValidationError: false, // Validation error not show on UI
      formReadOnly: false,
      formDisabled: false
    };
  }

  componentDidMount() {
    this._resolveValue(this.props);
    this._resolveReadOnly(this.props);
    this._resolveDisabled(this.props);
    this._resolveValidation(this.props);
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
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

  /**
   * Returns true if the string in the <value> argument contains some leading or trailing white-spaces
   * @return {bool}
   */
  isTrimmable(value) {
    if (!value || (typeof value !== 'string')) {
      return false;
    }
    const trimmedVal = value.trim();
    if (value !== trimmedVal) {
      return true;
    }
    return false;
  }

  _resolveValue(props) {
    const value = props.value != null ? this.normalizeValue(props.value) : null;
    this.setState({ value });
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
    this.setState({validation: this.getValidationDefinition(props.required, props.validation)}, () => {
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

  /**
  * Defines if component works with complex value.
  * That is using for correct set input value in form component.
  * Complex value could be exist in _embedded map and we need to now if
  * should be used value from field (UUID) or _embedded (entity).
  *
  */
  isValueComplex() {
    return false;
  }

  /**
   * Resolves required attribute validation
   *
   * @return {object} Joi validation
   */
  getRequiredValidationSchema() {
    return Joi.any().required();
  }

  /**
   * Automatically appends required Joi validation by their schema
   *
   * @param  {boolean} required
   * @return {object} Joi validation
   */
  getValidationDefinition(required, validation = null) {
    const _validation = validation || this.props.validation;
    //
    let _resultValidation;
    if (required === true) {
      _resultValidation = this.getRequiredValidationSchema(_validation);
      if (_validation) {
        _resultValidation = _validation.concat(_resultValidation);
      }
    } else {
      // this is default value for not required value
      const notMandatory = Joi.any().empty('');
      _resultValidation = _validation ? notMandatory.concat(_validation) : notMandatory;
    }
    return _resultValidation;
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
    let result = true;
    if (this.props.onChange) {
      result = this.props.onChange(event); // TODO: event value only?
    }
    // if onChange listener returns false, then we can end
    if (result === false) {
      return;
    }
    //
    this.setState({
      value: event.currentTarget.value
    }, () => {
      this.validate();
    });
  }

  /**
   * Localization validation errors
   * @param  {string} type   type of validation
   * @param  {json} params (key-value of validation params)
   * @return {string}        localized message
   */
  _localizationValidation(type, params) {
    return this.i18n(`validationError.${ type }`, params);
  }

  validate(showValidationError, cb) {
    const {value, validation} = this.state;
    const showValidations = showValidationError != null ? showValidationError : true;

    let result = null;
    if (validation) {
      result = validation.validate(value);
      // custom validate
      if (this.props.validate) {
        result = this.props.validate(value, result);
      }
      if (result && result.error) {
        // show validation error on UI
        this.setValidationResult(result, showValidations, cb);
        return false;
      }
    }
    //
    const softValidationResult = this.softValidationResult();
    if (softValidationResult) {
      this.setState({
        validationResult: softValidationResult
      });
      return true;
    }
    //
    this.setState({
      validationResult: {
        status: null,
        class: '',
        isValid: true,
        message: null,
        showValidationError: true
      },
      showValidationError: showValidations
    }, () => {
      if (cb) {
        cb(result);
      }
    }); // show validation error on UI
    return true;
  }

  /**
   * Set validation result for UI
   *
   * @since 11.0.0
   */
  setValidationResult(result, showValidations, cb = null) {
    if (!result || !result.error) {
      return;
    }
    //
    let message = null;
    let key;
    const params = {};
    if (result.error.message) { // fully localized message
      message = result.error.message;
    } else {
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
      message = this._localizationValidation(key, params);
    }
    //
    this.setState({
      validationResult: {
        status: 'error',
        class: 'has-error has-feedback',
        isValid: false,
        message
      },
      showValidationError: showValidations
    }, () => {
      if (cb) {
        cb(result);
      }
    });
  }

  softValidationResult() {
    return null;
  }

  getValue() {
    return this.state.value;
  }

  setValue(value, cb) {
    this.setState({
      value
    }, this.validate.bind(this, false, cb));
  }

  getBody() {
    return <div/>;
  }

  getValidationResult() {
    return this.state.validationResult;
  }

  /**
   * Help icon in label
   */
  renderHelpIcon() {
    // FIXME: layout has wrong padding => use flexbox instead
    return (
      <HelpIcon content={ this.props.help } style={{ marginLeft: 1 }}/>
    );
  }

  /**
   * Help block under input
   */
  renderHelpBlock() {
    const { helpBlock } = this.props;
    //
    if (!helpBlock) {
      return null;
    }
    return (
      <span className="help-block" style={{ whiteSpace: 'normal' }}>{helpBlock}</span>
    );
  }

  /**
   * Returns title placement for validations tooltips etc.
   *
   * @return {string} tittle position
   */
  getTitlePlacement() {
    return 'top';
  }

  _toValidationType(validationError) {
    if (!validationError) {
      return this._localizationValidation('invalid.base');
    }
    if (validationError.message) {
      return this.i18n(validationError.message, {
        min: validationError.minValue,
        max: validationError.maxValue,
        regex: validationError.regexValue,
        unique: validationError.uniqueValue,
        required: validationError.missingValue
      });
    }
    if (validationError.missingValue === true) {
      return this._localizationValidation('any.allowOnly');
    }
    if (validationError.minValue) {
      return this._localizationValidation('number.min', { count: validationError.minValue });
    }
    if (validationError.maxValue) {
      return this._localizationValidation('number.max', { count: validationError.maxValue });
    }
    if (validationError.regexValue) {
      return this._localizationValidation('invalid.regex', { regex: validationError.regexValue });
    }
    if (validationError.uniqueValue) {
      return this._localizationValidation('invalid.unique', { unique: validationError.uniqueValue });
    }
    return this._localizationValidation('invalid.base');
  }

  /**
   *  Returns title - could be shown in tooltips (validations etc)
   *
   * @return {string}
   */
  getTitle() {
    const { label, placeholder, tooltip, validationErrors, validationMessage } = this.props;
    const propertyName = label || placeholder;
    const validationResult = this.getValidationResult();
    //
    let title = null;
    if (validationErrors && validationErrors.length > 0) {
      validationErrors.forEach(validationError => {
        if (title) {
          title += ', ';
        } else {
          title = '';
        }
        title += `${ this._toValidationType(validationError) }`;
      });
    } else if (validationResult && validationResult.message) {
      title = validationMessage ? this.i18n(validationMessage) : `${ validationResult.message }`;
    } else if (!label) {
      title = propertyName;
    }
    if (tooltip) {
      title = !title ? tooltip : `${title} (${tooltip})`;
    }
    //
    return title;
  }

  render() {
    const { hidden, className, rendered, validationErrors } = this.props;
    //
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
        feedback = <Icon icon="warning-sign" className="form-control-feedback" />;
      }
      if (this.getValidationResult().status === 'warning') {
        feedback = <Icon icon="warning-sign" className="form-control-feedback" />;
        validationClass = 'has-warning has-feedback';
      }
    }
    if (validationErrors && validationErrors.length > 0) {
      feedback = <Icon icon="warning-sign" className="form-control-feedback" />;
      validationClass = 'has-error has-feedback';
    }
    return (
      <div className={ `${ _className } ${ validationClass }` } style={ this.props.style }>
        { this.getBody(feedback) }
      </div>
    );
  }
}

AbstractFormComponent.propTypes = {
  // ref: PropTypes.string.isRequired, VS: ref is mandator property,
  //  but is here problem with warning after first component render
  rendered: PropTypes.bool,
  value: PropTypes.any, // each generalization uses different type
  label: PropTypes.string,
  disabled: PropTypes.bool,
  hidden: PropTypes.bool,
  labelSpan: PropTypes.string, // defined span for label
  componentSpan: PropTypes.string, // defined span for component
  required: PropTypes.bool, // add default required validation and asterix
  readOnly: PropTypes.bool, // html readonly
  onChange: PropTypes.func,
  validation: PropTypes.object,
  /**
   * List of InvalidFormAttributeDto
   */
  validationErrors: PropTypes.arrayOf(PropTypes.object),
  /**
   * If message is not defined, then default message by invalid validation type will be shown.
   */
  validationMessage: PropTypes.string,
  validate: PropTypes.func, // function for custom validation (input is value and result from previous validations)
  style: PropTypes.object, // form-group element style
  notControlled: PropTypes.bool // if true, then is component not controlled by AbstractForm
};

AbstractFormComponent.defaultProps = {
  labelSpan: null,
  componentSpan: null,
  required: false,
  hidden: false,
  readOnly: false,
  disabled: false,
  rendered: true,
  notControlled: false,
  validationErrors: []
};
// usable in abstract form
AbstractFormComponent.__FormableComponent__ = true;

export default AbstractFormComponent;
