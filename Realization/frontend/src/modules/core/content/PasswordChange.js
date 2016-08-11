import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../../components/basic';
import { SecurityManager, IdentityManager } from '../../../modules/core/redux';
//
import help from './PasswordChange_cs.md';

const identityManager = new IdentityManager();
const securityManager = new SecurityManager();

class PasswordChange extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showLoading: false,
      forUsername: null
    };
  }

  componentDidMount() {
    this.selectNavigationItem('password-change');
    const { query } = this.props.location;
    const forUsername = (query) ? query.name : null;
    if (!forUsername) {
      this.refs.form.setData({});
      this.refs.username.focus();
    } else {
      this.setState({
        forUsername
      });
      this.refs.form.setData({
        username: forUsername
      });
      this.refs.passwordOld.focus();
    }
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

  _validatePassword(property, onlyValidate, value, result) {
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

  passwordChange(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    this.setState({
      showLoading: true
    });
    const username = this.refs.username.getValue();
    const password = this.refs.password.getValue();

    let passwordChangePromise;
    if (this.state.forUsername !== null) {
      passwordChangePromise = identityManager.getService().passwordMustChange(
        username,
        this.refs.passwordOld.getValue(),
        password
      );
    } else {
      passwordChangePromise = identityManager.getService().passwordChangePublic(
        username,
        this.refs.passwordOld.getValue(),
        password
      );
    }

    passwordChangePromise.then(response => {
      this.setState({
        showLoading: false
      });
      if (response.status === 204) {
        return {};
      }
      return response.json();
    })
    .then(json => {
      if (!json.error) {
        this.login(username, password);
        if (this.state.forUsername === null) {
          this.addMessage({ title: this.i18n('message.passwordChange.success.title'), message: this.i18n('message.passwordChange.success.message') });
        } else {
          this.addMessage({ title: this.i18n('message.passwordMustChange.success.title'), message: this.i18n('message.passwordMustChange.success.message') });
        }
      } else {
        this.addError(json.error);
      }
    })
    .catch(error => {
      this.addError(error);
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
    const { forUsername, showLoading } = this.state;

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <div className="row">
          <div className="col-sm-offset-4 col-sm-4">
            <form onSubmit={this.passwordChange.bind(this)}>
              <Basic.Panel showLoading={showLoading}>
                <Basic.PanelHeader text={this.i18n('header')} help={forUsername ? '' : help}/>

                <Basic.AbstractForm ref="form" className="form-horizontal panel-body">

                  <Basic.Alert text={this.i18n('message.passwordChange.info')} className="no-margin" rendered={!forUsername}/>

                  <Basic.Alert level="warning" text={this.i18n('message.passwordChange.required')} className="no-margin" rendered={forUsername !== null}/>

                  <Basic.TextField
                    ref="username"
                    label={this.i18n('entity.Identity.name')}
                    placeholder={this.i18n('entity.Identity.name')}
                    required
                    labelSpan="col-md-4"
                    componentSpan="col-md-8"/>
                  <Basic.TextField
                    type={'password'}
                    ref="passwordOld"
                    label={this.i18n('passwordOld')}
                    placeholder={this.i18n('passwordOld')}
                    required
                    labelSpan="col-md-4"
                    componentSpan="col-md-8"/>
                  <Basic.TextField
                    type={'password'}
                    ref="password"
                    label={this.i18n('password')}
                    placeholder={this.i18n('password')}
                    required
                    labelSpan="col-md-4"
                    componentSpan="col-md-8"
                    validate={this._validatePassword.bind(this, 'passwordAgain', true)}/>
                  <Basic.TextField
                    type={'password'}
                    ref="passwordAgain"
                    label={this.i18n('passwordAgain.label')}
                    placeholder={this.i18n('passwordAgain.placeholder')}
                    required
                    labelSpan="col-md-4"
                    componentSpan="col-md-8"
                    validate={this._validatePassword.bind(this, 'password', false)}
                    className="last"/>
                </Basic.AbstractForm>

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
