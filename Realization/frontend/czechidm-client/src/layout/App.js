import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Footer from './Footer';
import {Basic, Advanced, SecurityManager, Managers} from 'czechidm-core';

export class App extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.securityManager = new SecurityManager();
    this.configurationManager = new Managers.ConfigurationManager();
  }

  /**
  * Look out: This method is supposted to be aplication entry point
  */
  componentDidMount() {
    this.hideAllMessages();
    this.ping();
  }

  /**
  * Look out: This method is supposted to be aplication entry point
  */
  componentDidUpdate() {
    const { location, userContext } = this.props;
    // select navigation
    if (location.pathname === '/') {
      this.selectNavigationItem('home');
    }
    if (this.refs.form) { // when modal is closed, form is not defined
      this.refs.form.setData({
        username: userContext.username,
        password: this.refs.password.getValue() // prevent filled password
      });
    }
    this.ping();
    // onEnter makes this redirection now
    // console.log('router', this.context.router);
    // console.log('location', this.props.location);
    // check if is user logged, if not will do redirect to login page
    /* if (!this.props.userContext.isAuthenticated && this.props.location.pathname !== '/login') {
      this.context.router.replace('/login');
    }*/
  }

  ping() {
    const { publicConfigurations } = this.props;
    if (!publicConfigurations || publicConfigurations.size === null) {
      this.context.store.dispatch(this.configurationManager.fetchPublicConfigurations());
    }
  }

  handleSubmit(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const formData = this.refs.form.getData();
    // refresh login
    this.context.store.dispatch(this.securityManager.login(formData.username, formData.password));
  }

  render() {
    const { publicConfigurations, location, userContext, bulk } = this.props;
    //
    return (
      <div id="content-wrapper">
        <Basic.FlashMessages ref="messages"/>
        {
          ((!publicConfigurations || publicConfigurations.size === 0) && location.pathname !== '/unavailable')
          ?
          <Basic.Loading className="global" showLoading/>
          :
          <div>
            <Advanced.Navigation />
            <div id="content-container" className={SecurityManager.isAuthenticated(userContext) ? 'with-sidebar' : ''}>
              {this.props.children}
              {
                /* TODO: move to redux and hide it, when is needed */
                location.pathname !== '/login' && location.pathname !== '/password/reset' && location.pathname !== '/password/change'
                ?
                <Footer />
                :
                null
              }

              <Advanced.ModalProgressBar
                show={bulk.showLoading}
                text={bulk.action.title}
                count={bulk.size}
                counter={bulk.counter}/>

              <Basic.Modal dialogClassName="login-container" show={userContext.isExpired}>
                <form onSubmit={this.handleSubmit.bind(this)}>
                  <Basic.Modal.Header text={this.i18n('error.LOG_IN.title')} />
                  <Basic.Modal.Body>
                    <Basic.Loading showLoading={userContext.showLoading}>
                      <Basic.Alert text={this.i18n('error.LOG_IN.message')}/>
                      <Basic.AbstractForm ref="form" className="form-horizontal" style={{ padding: 0, backgroundColor: '#fff' }}>
                        <Basic.TextField
                          ref="username"
                          labelSpan="col-sm-5"
                          componentSpan="col-sm-7"
                          label={this.i18n('content.login.username')}
                          placeholder={this.i18n('content.login.username')}
                          required
                          readOnly/>
                        <Basic.TextField
                          type="password"
                          ref="password"
                          labelSpan="col-sm-5"
                          componentSpan="col-sm-7"
                          className="last"
                          label={this.i18n('content.login.password')}
                          placeholder={this.i18n('content.login.password')}
                          required/>
                      </Basic.AbstractForm>
                    </Basic.Loading>
                  </Basic.Modal.Body>
                  <Basic.Modal.Footer>
                    <Basic.Button
                      level="link"
                      onClick={() => this.context.router.push('/logout')}
                      showLoading={userContext.showLoading}
                      title={this.i18n('content.login.button.logout.title')}
                      titlePlacement="bottom">
                      {this.i18n('content.login.button.logout.value')}
                    </Basic.Button>
                    <Basic.Button type="submit" level="success" showLoading={userContext.showLoading}>
                      {this.i18n('content.login.button.login')}
                    </Basic.Button>
                  </Basic.Modal.Footer>
                </form>
              </Basic.Modal>
            </div>
          </div>
        }
      </div>
    );
  }
}

App.propTypes = {
  /**
   * Application public configuration loaded from BE
   */
  publicConfigurations: PropTypes.arrayOf(PropTypes.object),
  /**
   * Logged user context
   */
  userContext: PropTypes.object,
  /**
   * Globally bulk action
   */
  bulk: PropTypes.object
};

App.defaultProps = {
  publicConfigurations: null,
  userContext: null,
  bulk: { action: {} },
};

// Which props do we want to inject, given the global state?
// Note: use https://github.com/faassen/reselect for better performance.
function select(state) {
  return {
    userContext: state.security.userContext,
    publicConfigurations: Managers.DataManager.getData(state, Managers.ConfigurationManager.PUBLIC_CONFIGURATIONS),
    bulk: state.data.bulk
  };
}

// Wrap the component to inject dispatch and state into it
export default connect(select)(App);
