import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../components/basic';
import * as Advanced from '../components/advanced';
import * as Utils from '../utils';
import { SecurityManager, IdentityManager } from '../redux';
import help from './PasswordChange_cs.md';
import ValidationMessage from './identity/ValidationMessage';

const identityManager = new IdentityManager();
const securityManager = new SecurityManager();

class PasswordChange extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showLoading: false
    };
  }

  componentDidMount() {
    this.selectNavigationItem('password-change');
    this.refs.form.setData({});
    this.refs.username.focus();
  }

  getContentKey() {
    return 'content.password.change';
  }

  cancel() {
    const { userContext } = this.props;
    if (!SecurityManager.isAuthenticated(userContext)) {
      this.context.router.push('/login');
    } else {
      this.context.router.push('/logout');
    }
  }

  /**
   * Method set value to component PasswordField
   */
  _initPasswordFields(value) {
    this.refs.passwords.setValue(value);
  }

  passwordChange(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid() || !this.refs.passwords.validate()) {
      return;
    }
    this.setState({
      showLoading: true
    });
    const username = this.refs.username.getValue();
    const oldPassword = this.refs.passwordOld.getValue();
    const password = this.refs.passwords.getValue();

    identityManager.getService().passwordChange(username, {
      oldPassword,
      newPassword: password,
      all: true, // change in idm and in all accounts
      resources: []
    }, false)
    .then(response => {
      this.setState({
        showLoading: false
      });
      if (response.status === 404) {
        this._initPasswordFields(password);
        throw new Error('IDENTITY_NOT_FOUND');
      }
      if (response.status === 204) {
        return {};
      }
      return response.json();
    })
    .then(json => {
      if (Utils.Response.hasError(json)) {
        this._initPasswordFields(password);
        const error = Utils.Response.getFirstError(json);
        this.setState({
          validationError: error
        });
        throw error;
      }
      return json;
    })
    .then(() => {
      this.setState({
        validationError: null
      });
      this.login(username, password);
      this.addMessage({ title: this.i18n('message.passwordChange.success.title'), message: this.i18n('message.passwordChange.success.message') });
    })
    .catch(error => {
      if (error.message === 'IDENTITY_NOT_FOUND') {
        this.addMessage({
          level: 'warning',
          title: this.i18n('error.PASSWORD_CHANGE_FAILED.title'),
          message: this.i18n('error.IDENTITY_NOT_FOUND.message', { identity: username }),
        });
      } else {
        this.addError(error);
      }
      this.refs.passwords.setValue(password);
    });
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

  render() {
    const { showLoading, validationError } = this.state;

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <div className="row">
          <div className="col-sm-offset-4 col-sm-4">
            <form onSubmit={this.passwordChange.bind(this)}>
              <Basic.Panel showLoading={showLoading}>
                <Basic.PanelHeader text={this.i18n('header')} help={help}/>

                <Basic.AbstractForm ref="form" className="panel-body">

                  <Basic.Alert text={this.i18n('message.passwordChange.info')} className="no-margin"/>

                  <Basic.TextField
                    ref="username"
                    label={this.i18n('entity.Identity.username')}
                    placeholder={this.i18n('entity.Identity.username')}
                    required/>
                  <Basic.TextField
                    type={'password'}
                    ref="passwordOld"
                    label={this.i18n('passwordOld')}
                    placeholder={this.i18n('passwordOld')}
                    required/>
                  <Advanced.PasswordField
                    className="form-control"
                    ref="passwords"/>
                </Basic.AbstractForm>
                <Basic.Panel className="no-border last">
                  <ValidationMessage error={validationError} />
                </Basic.Panel>
                <Basic.PanelFooter>
                  <Basic.Button level="link" onClick={this.cancel.bind(this)}>
                    {this.i18n('button.cancel')}
                  </Basic.Button>
                  <Basic.Button type="submit" level="success">
                    {this.i18n('button.passwordChange')}
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

PasswordChange.propTypes = {
  userContext: React.PropTypes.object
};
PasswordChange.defaultProps = {
  userContext: null
};

function select(state) {
  return {
    userContext: state.security.userContext
  };
}

export default connect(select)(PasswordChange);
