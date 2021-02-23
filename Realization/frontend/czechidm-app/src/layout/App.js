import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import classNames from 'classnames';
import { Route } from 'react-router-dom';
import Joi from 'joi';
//
import { Basic, Advanced, Managers } from 'czechidm-core';
import Dashboard from 'czechidm-core/src/content/Dashboard';
import Footer from './Footer';
//
const securityManager = new Managers.SecurityManager();
const dataManager = new Managers.DataManager();

/**
 * Application entry point.
 *
 * @author Radek Tomiška
 * @author Roman Kučera
 */
export class App extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      isLogout: false,
      showTwoFactor: false,
      token: null
    };
    context.history = props.history;
  }

  /**
  * Look out: This method is aplication entry point
  */
  componentDidUpdate() {
    const { location, userContext, appReady, casEnabled } = this.props;

    /**
     * Perform logout from IdM if token is expired so IdM will redirect to /login, perform auth against CAS and user will be signed in again
     */
    if (userContext.isExpired && casEnabled) {
      this.context.store.dispatch(securityManager.logout(() => {
        this.context.history.replace('/login');
      }));
    }

    // select navigation
    if (location.pathname === '/') {
      this.selectNavigationItem('home');
    }
    if (this.refs.form) { // when modal is closed, form is not defined
      if (this.refs.password) {
        this.refs.form.setData({
          username: userContext.username,
          password: this.refs.password.getValue() // preserve filled password
        });
        this.refs.password.focus();
      }
    }

    if (appReady) {
      this._handleTokenRefresh();
    }
  }

  login(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const formData = this.refs.form.getData();
    // refresh login
    this.context.store.dispatch(securityManager.login(formData.username, formData.password, (result, error) => {
      if (error && error.statusEnum) {
        if (error.statusEnum === 'MUST_CHANGE_IDM_PASSWORD') {
          this.context.store.dispatch(dataManager.storeData(Managers.SecurityManager.PASSWORD_MUST_CHANGE, formData.password));
          this.context.history.replace(`/password/change?username=${ formData.username }`);
        }
        if (error.statusEnum === 'TWO_FACTOR_AUTH_REQIURED') {
          this.setState({
            showTwoFactor: true,
            token: error.parameters.token,
            username: formData.username,
            password: formData.password
          });
        }
      }
    }));
  }

  handleTwoFactor(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const formData = this.refs.form.getData();
    const verificationCode = formData.verificationCode;
    const { token } = this.state;
    this.context.store.dispatch(securityManager.loginTwoFactor({ token, verificationCode }, (result, error) => {
      if (error) {
        if (error.statusEnum && error.statusEnum === 'MUST_CHANGE_IDM_PASSWORD') {
          this.setState({
            showTwoFactor: false
          }, () => {
            this.context.store.dispatch(dataManager.storeData(Managers.SecurityManager.PASSWORD_MUST_CHANGE, this.state.password));
            this.context.history.replace(`/password/change?username=${ this.state.username }`);
          });
        }
      } else {
        this.setState({
          showTwoFactor: false
        });
      }
    }));
  }

  logout(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      isLogout: true
    }, () => {
      this.context.store.dispatch(securityManager.logout(() => {
        this.context.history.push('/login');
        this.setState({
          isLogout: false
        });
      }));
    });
  }

  /**
   * Creates react-router Routes components for this component (url).
   * And add Dashboard route.
   */
  generateRouteComponents() {
    const basicRoutes = super.generateRouteComponents();
    const mockRoute = {component: Dashboard, access: [{ type: 'IS_AUTHENTICATED' }]};
    return [<Route key="dashboard" exact path="/" component={this._getComponent(mockRoute)}/>, ...basicRoutes];
  }

  _handleTokenRefresh() {
    const { userContext } = this.props;
    // handle token expiration extension
    if (userContext) {
      this.context.store.dispatch(securityManager.checkRefreshedToken());
    }
  }

  render() {
    const { userContext, bulk, appReady, navigationCollapsed, hideFooter, casEnabled } = this.props;
    const { isLogout, showTwoFactor } = this.state;
    const titleTemplate = `%s | ${ this.i18n('app.name') }`;
    const classnames = classNames(
      { 'with-sidebar': !userContext.isExpired && Managers.SecurityManager.isAuthenticated(userContext) },
      { collapsed: navigationCollapsed }
    );
    // @todo-upgrade-10 - FlashMessages throw warning "Function components cannot be given refs.
    // Attempts to access this ref will fail. Did you mean to use React.forwardRef()?"
    return (
      <div id="content-wrapper">
        <Basic.FlashMessages ref="messages" />
        {
          !appReady
          ?
          <Basic.Loading className="global" showLoading />
          :
          <Basic.Div>
            <Helmet title={ this.i18n('navigation.menu.home') } titleTemplate={ titleTemplate } />
            <Advanced.Navigation />
            <div id="content-container" className={ classnames }>
              {
                userContext.isExpired
                ||
                isLogout
                ||
                (
                  <Basic.Div>
                    { /* Childrens are hiden, when token expires =>
                      all components are loaded (componentDidMount) after identity is logged again */ }
                    { this.getRoutes() }
                    <Footer rendered={ !hideFooter } />
                  </Basic.Div>
                )
              }
              <Advanced.ModalProgressBar
                show={ bulk.showLoading }
                text={ bulk.action.title }
                count={ bulk.size }
                counter={ bulk.counter }
              />
              <Basic.Modal dialogClassName="login-container" show={ userContext.isExpired }>
                <Basic.Div rendered={ showTwoFactor }>
                  <form onSubmit={ this.handleTwoFactor.bind(this) }>
                    <Basic.Modal.Header text={ this.i18n('content.login.twoFactor.header') }/>
                    <Basic.Modal.Body>
                      <Basic.Loading showLoading={ userContext.showLoading }>
                        <Basic.AbstractForm ref="form" className="form-horizontal" style={{ padding: 0, backgroundColor: '#fff' }}>
                          <Basic.TextField
                            ref="verificationCode"
                            labelSpan="col-sm-5"
                            componentSpan="col-sm-7"
                            className="last"
                            label={ this.i18n('content.login.twoFactor.verificationCode.label') }
                            placeholder={ this.i18n('content.login.twoFactor.verificationCode.placeholder') }
                            required
                            validation={ Joi.number().integer().min(0).max(999999) }/>
                        </Basic.AbstractForm>
                      </Basic.Loading>
                    </Basic.Modal.Body>
                    <Basic.Modal.Footer>
                      <Basic.Button
                        level="link"
                        onClick={ () => this.logout() }
                        title={ this.i18n('content.login.button.logout.title') }
                        titlePlacement="bottom">
                        { this.i18n('content.login.button.logout.value') }
                      </Basic.Button>
                      <Basic.Button type="submit" level="success" showLoading={ userContext.showLoading }>
                        { this.i18n('button.verify.label') }
                      </Basic.Button>
                    </Basic.Modal.Footer>
                  </form>
                </Basic.Div>

                <Basic.Div rendered={ !showTwoFactor && !casEnabled }>
                  <form onSubmit={ this.login.bind(this) }>
                    <Basic.Modal.Header text={ this.i18n('error.LOG_IN.title') } />
                    <Basic.Modal.Body>
                      <Basic.Loading showLoading={ userContext.showLoading }>
                        <Basic.Alert text={ this.i18n('error.LOG_IN.message') } />
                        <Basic.AbstractForm
                          ref="form"
                          data={{
                            username: userContext.username,
                            password: this.refs.password ? this.refs.password.getValue() : null
                          }}
                          className="form-horizontal"
                          style={{ padding: 0, backgroundColor: '#fff' }}>
                          <Basic.TextField
                            ref="username"
                            labelSpan="col-sm-5"
                            componentSpan="col-sm-7"
                            label={ this.i18n('content.login.username') }
                            placeholder={ this.i18n('content.login.username') }
                            required
                            readOnly
                          />
                          <Basic.TextField
                            type="password"
                            ref="password"
                            labelSpan="col-sm-5"
                            componentSpan="col-sm-7"
                            className="last"
                            label={ this.i18n('content.login.password') }
                            placeholder={ this.i18n('content.login.password') }
                            required
                          />
                        </Basic.AbstractForm>
                      </Basic.Loading>
                    </Basic.Modal.Body>
                    <Basic.Modal.Footer>
                      <Basic.Button
                        level="link"
                        onClick={ () => this.logout() }
                        title={ this.i18n('content.login.button.logout.title') }
                        titlePlacement="bottom">
                        { this.i18n('content.login.button.logout.value') }
                      </Basic.Button>
                      <Basic.Button type="submit" level="success" showLoading={ userContext.showLoading }>
                        { this.i18n('content.login.button.login') }
                      </Basic.Button>
                    </Basic.Modal.Footer>
                  </form>
                </Basic.Div>
              </Basic.Modal>
            </div>
          </Basic.Div>
        }
      </div>
    );
  }
}

App.propTypes = {
  /**
   * Logged user context
   */
  userContext: PropTypes.object,
  /**
   * Globally bulk action
   */
  bulk: PropTypes.object,
  appReady: PropTypes.bool,
  i18nReady: PropTypes.string,
  navigationCollapsed: PropTypes.bool,
  /**
   * Footer will be hidden
   */
  hideFooter: PropTypes.bool
};

App.defaultProps = {
  userContext: null,
  bulk: { action: {} },
  appReady: false,
  i18nReady: null,
  navigationCollapsed: false,
  hideFooter: false
};

// Which props do we want to inject, given the global state?
// Note: use https://github.com/faassen/reselect for better performance.
function select(state) {
  return {
    userContext: state.security.userContext,
    bulk: state.data.bulk,
    appReady: state.config.get('appReady'),
    i18nReady: state.config.get('i18nReady'),
    navigationCollapsed: state.security.userContext.navigationCollapsed,
    hideFooter: state.config.get('hideFooter'),
    casEnabled: Managers.ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.core.cas.sso.enabled', false)
  };
}

// Wrap the component to inject dispatch and state into it
export default connect(select)(App);
