

import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../components/basic';
import { SecurityManager } from '../../../modules/core/redux';

const securityManager = new SecurityManager();

class Login extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.login';
  }

  _redirectIfIsAuthenticated() {
    if (this.props.userContext.isAuthenticated) {
      // redirection to requested page before login
      const { location } = this.props;
      if (location.state && location.state.nextPathname) {
        this.context.router.replace(location.state.nextPathname);
      } else {
        this.context.router.replace('/');
      }
    }
  }

  componentDidMount() {
    this._redirectIfIsAuthenticated();
    this.selectNavigationItem('home');
    this.refs.form.setData({});
    this.refs.username.focus();
  }

  componentDidUpdate() {
    this._redirectIfIsAuthenticated();
  }

  handleSubmit(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const formData = this.refs.form.getData();
    this.context.store.dispatch(securityManager.login(formData.username, formData.password));
  }

  passwordReset() {
    this.context.router.push('/password/reset');
  }

  passwordChange() {
    this.context.router.push('/password/change');
  }

  render() {
    const { userContext } = this.props;
    //
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <div >
          <form onSubmit={this.handleSubmit.bind(this)}>
            <Basic.Panel className="login-container" showLoading={userContext.showLoading}>
              <Basic.PanelHeader text={this.i18n('header')}/>
              <Basic.PanelBody>
                <Basic.AbstractForm ref="form" style={{ padding: 0, backgroundColor: '#fff' }}>
                  <Basic.TextField
                    ref="username"
                    labelSpan="col-sm-5"
                    componentSpan="col-sm-7"
                    label={this.i18n('username')}
                    placeholder={this.i18n('username')}
                    required/>
                  <Basic.TextField
                    type="password"
                    ref="password"
                    labelSpan="col-sm-5"
                    componentSpan="col-sm-7"
                    className="last"
                    label={this.i18n('password')}
                    placeholder={this.i18n('password')}
                    required/>
                </Basic.AbstractForm>
              </Basic.PanelBody>
              <Basic.PanelFooter>
                <Basic.Button type="submit" level="success">
                  {this.i18n('button.login')}
                </Basic.Button>
              </Basic.PanelFooter>
            </Basic.Panel>
          </form>
        </div>
      </div>
    );
  }
}

Login.propTypes = {
  userContext: PropTypes.object
}

Login.defaultProps = {
  userContext: { isAuthenticated: false }
}

function select(state) {
  return {
    userContext: state.security.userContext
  }
}

export default connect(select)(Login)
