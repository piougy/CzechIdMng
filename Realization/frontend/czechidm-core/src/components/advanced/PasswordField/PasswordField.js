import React, { PropTypes } from 'react';
//
import * as Basic from '../../basic';

/**
 * Component with two TextField and password estimator.
 */
class PasswordField extends Basic.AbstractFormComponent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      passwordForValidation: null
    };
  }

  componentWillReceiveProps(nextProps) {
    this.refs.newPassword.setValue(nextProps.newPassword);
    this.refs.newPasswordAgain.setValue(nextProps.newPasswordAgain);
    this._updatePasswordForValidation(nextProps.newPassword);
  }

  getValue() {
    return this.refs.newPassword.getValue();
  }

  _updatePasswordForValidation(value) {
    let passwordForValidation = '';

    // if exist value, set passwordForValidation, other way set empty string
    if (value) {
      passwordForValidation = value;
    }

    this.setState({
      passwordForValidation
    });
  }

  validate(showValidationError) {
    const showValidations = showValidationError != null ? showValidationError : true;
    if (!this.refs.newPassword.validate() || !this.refs.newPasswordAgain.validate()) {
      return false;
    }

    return showValidations;
  }

  _validatePassword(property, onlyValidate, value, result) {
    if (property === 'newPasswordAgain') {
      this._updatePasswordForValidation(this.refs.newPassword.getValue());
    }

    if (onlyValidate) {
      this.refs[property].validate();
      return result;
    }
    if (result.error) {
      return result;
    }
    const opositeValue = this.refs[property].getValue();
    if (opositeValue !== value) {
      return {error: {key: 'passwords_not_same'}};
    }
    return result;
  }

  render() {
    const { newPassword, newPasswordAgain, type, required, readOnly, labelSpan, componentSpan } = this.props;
    const { passwordForValidation } = this.state;

    return (
      <div>
        <Basic.TextField type={type} ref="newPassword" value={newPassword}
          validate={this._validatePassword.bind(this, 'newPasswordAgain', true)} readOnly={readOnly}
          label={this.i18n('content.password.change.password')} required={required}
          labelSpan={labelSpan}
          componentSpan={componentSpan}
          style={{ marginBottom: 0 }}/>
        <div className="form-group" style={{ margin: '0 -13px' }}>
          { /* TODO: labelSpan vs offset - see public password change in small resolution - will be fixed with form-horizontal removal in the future */
            !labelSpan
            ||
            <span className={labelSpan}></span>
          }
          <Basic.PasswordStrength
            max={5}
            initialStrength={1}
            opacity={1}
            value={passwordForValidation}
            isIcon={false}
            spanClassName={componentSpan} />
        </div>
        <Basic.TextField type={type} ref="newPasswordAgain"
          value={newPasswordAgain} readOnly={readOnly}
          validate={this._validatePassword.bind(this, 'newPassword', false)}
          label={this.i18n('content.password.change.passwordAgain.label')} required={required}
          labelSpan={labelSpan}
          componentSpan={componentSpan}/>
      </div>
    );
  }
}

PasswordField.propTypes = {
  newPassword: PropTypes.string,
  newPasswordAgain: PropTypes.string,
  readOnly: PropTypes.bool,
  type: PropTypes.string,
  required: PropTypes.bool
};

PasswordField.defaultProps = {
  newPassword: '',
  readOnly: false,
  newPasswordAgain: '',
  type: 'password',
  required: true
};


export default PasswordField;
