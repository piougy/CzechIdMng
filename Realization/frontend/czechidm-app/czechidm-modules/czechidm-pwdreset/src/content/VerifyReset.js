import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';

import { Basic, Advanced } from 'czechidm-core';

import { PasswordResetService } from '../services';

import ValidationMessage from 'czechidm-core/src/content/identity/ValidationMessage';
import SecurityManager from 'czechidm-core/src/redux/security/SecurityManager';

const pwdReseService = new PasswordResetService();
const securityManager = new SecurityManager();


/**
 * Continue with password reset
 *
 * @author Peter Sourek
 */
class VerifyReset extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showLoading: false
    };
  }

  componentDidMount() {
    this.selectNavigationItems('pwdreset');
  }

  back() {
    this.context.router.push('/');
  }

  cancel() {
    this.context.router.push('/login');
  }

  passwordReset(event) {
    if (event) {
      event.preventDefault();
    }

    const { query } = this.props.location;
    const verificationToken = (query) ? query.verificationToken : null;

    if (!this.refs.formReset.isFormValid() || !this.refs.recaptcha.validate()) {
      return;
    }
    this.setState({
      showLoading: true
    });
    //
    this.context.store.dispatch(
      pwdReseService.changePasswordAfterVerification(
         'verificationToken=' + (verificationToken ? verificationToken : this.refs.token.getValue()),
        {
          newPassword: this.refs.passwords.getValue()
        }
      , this._afterSave.bind(this))
    );
  }

  login(username, password) {
    this.setState({
      showLoading: true
    });

    this.context.store.dispatch(securityManager.login(username, password, (isAuthenticated) => {
      this.setState({
        showLoading: false
      });

      if (!isAuthenticated) {
        return;
      }
      // redirection to requested page before login
      const { location } = this.props;
      if (location.state && location.state.nextPathname) {
        this.context.router.replace(location.state.nextPathname);
      } else {
        // TODO: user defined home page ...
        this.context.router.replace('/');
      }
    }));
  }

  _initPasswordFields(value) {
    this.refs.passwords.setValue(value);
  }

  /**
  * Just set showloading to false and set processEnded to form.
  * Call after save/create
  */
  _afterSave(entity, error, password) {
    if (error) {
      this.setState({
        showLoading: false,
        validationError: error
      }, this.refs.formReset.processEnded());
      this.addError(error);
      this._initPasswordFields(password);
    } else {
      this.addMessage({ message: this.i18n('pwdreset:passwordChanged') });
      this.login(entity.resolvedUsername, password);
    }
  }

  render() {
    const { validationError, showLoading, hasToken } = this.state;
    const { query } = this.props.location;
    const verificationToken = (query) ? query.verificationToken : null;

    return (
      <div>
        <Helmet title={this.i18n('pwdreset:title')} />
        <div className="row">
          <div className="col-sm-offset-4 col-sm-4">
            <form onSubmit={this.passwordReset.bind(this)}>
              <Basic.Panel rendered={hasToken} showLoading={showLoading}>
                <Basic.PanelHeader text={this.i18n('pwdreset:header')}/>

                <Basic.AbstractForm ref="formReset" className="panel-body">
                  <Basic.Row>
                    <Basic.Col lg={12}>
                      <Advanced.PasswordField
                        className="form-control"
                        ref="passwords"
                        type="password"
                        required
                        readOnly={false}
                        />
                      </Basic.Col>
                  </Basic.Row>
                  { !verificationToken &&
                  <Basic.Row>
                    <Basic.Col lg={12}>
                      <Basic.TextField
                      type={'input'}
                      ref="token"
                      label={this.i18n('pwdreset:token')}
                      placeholder={this.i18n('pwdreset:token')}
                      required/>
                    </Basic.Col>
                  </Basic.Row>
                  }
                  <Basic.Row>
                    <Basic.Col lg={12}>
                      <Advanced.Recaptcha
                        ref="recaptcha"
                        required
                      />
                    </Basic.Col>
                  </Basic.Row>
                </Basic.AbstractForm>
                <Basic.Panel className="no-border last">
                  { validationError &&
                  <ValidationMessage error={validationError} />
                  }
                </Basic.Panel>
                <Basic.PanelFooter>
                  <Basic.Button level="link" onClick={this.cancel.bind(this)}>
                    {this.i18n('button.cancel')}
                  </Basic.Button>
                  <Basic.Button type="submit" level="success">
                    {this.i18n('pwdreset:button.password-set')}
                  </Basic.Button>
                </Basic.PanelFooter>
              </Basic.Panel>
            </form>
          </div>
        </div>
      </div>
    );
  }
  }

VerifyReset.propTypes = {
  /**
   * Shows username input
   */
  showLogin: PropTypes.bool
};
VerifyReset.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(VerifyReset);
