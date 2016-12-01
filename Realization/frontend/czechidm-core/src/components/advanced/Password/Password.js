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

  getNewPassword() {
    return this.refs.newPassword.getValue();
  }

  getNewPasswordAgain() {
    return this.refs.newPasswordAgain.getValue();
  }

  _updateStrengthEstimator(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      passwordForValidation: this.refs.newPassword.getValue()
    });
  }

  render() {
    const { validate, newPassword, newPasswordAgain } = this.props;
    const { passwordForValidation } = this.state;
    return (
      <div>
        <Basic.TextField type="password" ref="newPassword" value={newPassword}
          validate={validate.bind(this, 'newPassword', false)}
          onChange={this._updateStrengthEstimator.bind(this)}
          label={this.i18n('content.password.change.password')} required/>
        <div className="form-group">
          <Basic.StrengthEstimator
            max={5}
            initialStrength={1}
            opacity={1}
            value={passwordForValidation}
            isIcon={false}/>
        </div>
        <Basic.TextField type="password" ref="newPasswordAgain"
          value={newPasswordAgain}
          validate={validate.bind(this, 'newPassword', false)}
          label={this.i18n('content.password.change.passwordAgain.label')} required/>
      </div>
    );
  }
}

Password.propTypes = {
  newPassword: PropTypes.string,
  newPasswordAgain: PropTypes.string,
  validate: PropTypes.func
};

Password.defaultProps = {
  newPassword: '',
  newPasswordAgain: ''
};


export default Password;
