import React, { PropTypes } from 'react';
//
import * as Basic from '../../basic';

/**
 * Simple date formatter with default format from localization
 */
class Password extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      passwordForValidation: null
    };
  }

  componentWillReceiveProps(nextProps) {
    this.refs.newPassword.setValue(nextProps.newPassword);
    this.refs.newPasswordAgain.setValue(nextProps.newPasswordAgain);
    this._updateStrengthEstimator(null, nextProps.newPassword);
  }

  getNewPassword() {
    return this.refs.newPassword.getValue();
  }

  getNewPasswordAgain() {
    return this.refs.newPasswordAgain.getValue();
  }

  _updateStrengthEstimator(event, value) {
    if (event) {
      event.preventDefault();
    }
    let passwordForValidation = null;

    if (value) {
      passwordForValidation = value;
    } else {
      passwordForValidation = this.refs.newPassword.getValue();
    }

    this.setState({
      passwordForValidation
    });
  }

  render() {
    const { validate, newPassword, newPasswordAgain, type, required, readOnly, labelSpan, componentSpan } = this.props;
    const { passwordForValidation } = this.state;

    return (
      <div>
        <Basic.TextField type={type} ref="newPassword" value={newPassword}
          validate={validate.bind(this, 'newPassword', false)}
          onChange={this._updateStrengthEstimator.bind(this)} readOnly={readOnly}
          label={this.i18n('content.password.change.password')} required={required}
          labelSpan={labelSpan}
          componentSpan={componentSpan}/>
        <div className="form-group">
          {
            !labelSpan
            ||
            <span className={labelSpan}></span>
          }
          <Basic.StrengthEstimator
            max={5}
            initialStrength={1}
            opacity={1}
            value={passwordForValidation}
            isIcon={false}
            spanClassName={componentSpan} />
        </div>
        <Basic.TextField type={type} ref="newPasswordAgain"
          value={newPasswordAgain} readOnly={readOnly}
          validate={validate.bind(this, 'newPassword', false)}
          label={this.i18n('content.password.change.passwordAgain.label')} required={required}
          labelSpan={labelSpan}
          componentSpan={componentSpan}/>
      </div>
    );
  }
}

Password.propTypes = {
  newPassword: PropTypes.string,
  newPasswordAgain: PropTypes.string,
  readOnly: PropTypes.bool,
  validate: PropTypes.func,
  type: PropTypes.string,
  required: PropTypes.bool,
  labelSpan: PropTypes.string,
  componentSpan: PropTypes.string
};

Password.defaultProps = {
  newPassword: '',
  readOnly: false,
  newPasswordAgain: '',
  type: 'password',
  required: true
};


export default Password;
