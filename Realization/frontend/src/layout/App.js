import React from 'react';
import { connect } from 'react-redux';
import Footer from './Footer';
import * as Basic from '../components/basic';
import * as Advanced from '../components/advanced';
import { SettingManager } from '../redux';
import { SecurityManager } from '../modules/core/redux';

export class App extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.settingManager = new SettingManager();
    this.securityManager = new SecurityManager();
  }

  /**
  * Look out: This method is supposted to be aplication entry point
  */
  componentDidMount() {
    const { setting } = this.props;
    this.hideAllMessages();
    if (!setting || setting.isEmpty()) {
      // this.context.store.dispatch(this.settingManager.fetchEntities());
    }
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
    // onEnter makes this redirection now
    // console.log('router', this.context.router);
    // console.log('location', this.props.location);
    // check if is user logged, if not will do redirect to login page
    /* if (!this.props.userContext.isAuthenticated && this.props.location.pathname !== '/login') {
      this.context.router.replace('/login');
    }*/
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
    const { setting, location, userContext, bulk } = this.props;
    //
    return (
      <div id="content-wrapper">
        <Basic.FlashMessages ref="messages"/>
        {
          (1 !== 1 && (!setting || setting.isEmpty()) && location.pathname !== '/unavailable')
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
                      <Basic.AbstractForm ref="form" style={{ padding: 0, backgroundColor: '#fff' }}>
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
   * Application setting loaded from BE
   */
  setting: React.PropTypes.object,
  /**
   * Logged user context
   */
  userContext: React.PropTypes.object,
  /**
   * Globally bulk action
   */
  bulk: React.PropTypes.object
};

App.defaultProps = {
  setting: null,
  userContext: null,
  bulk: { action: {} },
};

// Which props do we want to inject, given the global state?
// Note: use https://github.com/faassen/reselect for better performance.
function select(state) {
  return {
    userContext: state.security.userContext,
    setting: state.data.entity.Setting,
    bulk: state.data.bulk
  };
}

// Wrap the component to inject dispatch and state into it
export default connect(select)(App);
