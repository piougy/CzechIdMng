import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import classNames from 'classnames';
import {Route } from 'react-router-dom';

//
import { Basic, Advanced, Managers } from 'czechidm-core';
import Dashboard from 'czechidm-core/src/content/Dashboard';
import Footer from './Footer';
//
const securityManager = new Managers.SecurityManager();

/**
 * Application entry point
 *
 * @author Radek Tomiška
 */
export class App extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      isLogout: false
    };
    context.history = props.history;
  }

  /**
  * Look out: This method is aplication entry point
  */
  componentDidUpdate() {
    const { location, userContext, appReady } = this.props;
    // select navigation
    if (location.pathname === '/') {
      this.selectNavigationItem('home');
    }
    if (this.refs.form) { // when modal is closed, form is not defined
      this.refs.form.setData({
        username: userContext.username,
        password: this.refs.password.getValue() // preserve filled password
      });
      this.refs.password.focus();
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
    this.context.store.dispatch(securityManager.login(formData.username, formData.password));
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
    const { userContext, bulk, appReady, navigationCollapsed, hideFooter } = this.props;
    const { isLogout } = this.state;
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
          (
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
                      {/* Childrens are hiden, when token expires =>
                        all components are loaded (componentDidMount) after identity is logged again */}
                      {this.getRoutes()}
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
                </Basic.Modal>
              </div>
            </Basic.Div>
          )
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
    hideFooter: state.config.get('hideFooter')
  };
}

// Wrap the component to inject dispatch and state into it
export default connect(select)(App);
