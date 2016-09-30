import _ from 'lodash';
import { AuthenticateService, LocalizationService } from '../../services';
import FlashMessagesManager from '../flash/FlashMessagesManager';

/**
 * action types
 */
export const REQUEST_LOGIN = 'REQUEST_LOGIN';
export const RECEIVE_LOGIN = 'RECEIVE_LOGIN';
export const RECEIVE_LOGIN_EXPIRED = 'RECEIVE_LOGIN_EXPIRED';
export const RECEIVE_LOGIN_ERROR = 'RECEIVE_LOGIN_ERROR';
export const LOGOUT = 'LOGOUT';
//
const TOKEN_COOKIE_NAME = 'XSRF-TOKEN';

const authenticateService = new AuthenticateService();

/**
 * Encapsulate user context / authentication and authorization (commig soon)
 */
export default class SecurityManager {

  constructor() {
    this.flashMessagesManager = new FlashMessagesManager();
  }

  /**
   * Login user
   * @param  {string} username
   * @param  {string} password
   * @param  {function} redirect
   * @return {action}
   */
  login(username, password, redirect) {
    return (dispatch, getState) => {
      dispatch(this.requestLogin());
      dispatch(this.flashMessagesManager.hideAllMessages());
      //
      authenticateService.login(username, password)
      .then(json => {
        getState().logger.debug('logged user', json);
        // resolve authorities from auth
        const authorities = json.authentication.authorities.map(authority => { return authority.authority; });
        getState().logger.debug('logged user authorities', authorities);
        // construct logged user context
        const userContext = {
          username: json.username,
          isAuthenticated: true,
          tokenCIDMST: json.token,
          tokenCSRF: authenticateService.getCookie(TOKEN_COOKIE_NAME),
          authorities
        };
        dispatch(this.receiveLogin(userContext, redirect));
      })
      .catch(error => {
        dispatch(this.receiveLoginError(error, redirect));
      });
    };
  }

  /*
  * Request data
  */
  requestLogin() {
    return {
      type: REQUEST_LOGIN
    };
  }

  receiveLogin(userContext, redirect) {
    return (dispatch) => {
      // getState().logger.debug('received login', userContext);
      // redirect after login, if needed
      if (redirect) {
        redirect(userContext.isAuthenticated);
      }
      dispatch({
        type: RECEIVE_LOGIN,
        userContext
      });
    };
  }

  receiveLoginError(error, redirect) {
    return dispatch => {
      authenticateService.logout();
      // add error message
      if (error) {
        dispatch(this.flashMessagesManager.addErrorMessage({ position: 'tc' }, error));
      }
      // redirect after login, if needed
      if (redirect) {
        redirect(false);
      }
      dispatch({
        type: RECEIVE_LOGIN_ERROR
      });
    };
  }

  /**
   * Logout from BE and FE. Adds logout message after BE returns response
   *
   * @param  {function} redirect Callback, when loggout is done
   * @return {action}
   */
  logout(redirect) {
    return dispatch => {
      authenticateService.logout();
      dispatch(this.flashMessagesManager.hideAllMessages());
      dispatch(this.flashMessagesManager.addMessage({key: 'login', message: LocalizationService.i18n('content.logout.message.logout'), level: 'info', position: 'tc'}));
      dispatch(this.receiveLogout());
      if (redirect) {
        redirect();
      }
    };
  }

  /**
   * Logout without wait for promise result
   */
  logoutImmediatelly() {
    authenticateService.logout();
    return dispatch => {
      dispatch(this.receiveLogout());
    };
  }

  receiveLogout() {
    return {
      type: LOGOUT
    };
  }

  getCookie(cookieName) {
    return authenticateService.getCookie(cookieName);
  }

  /**
   * Puts new token to userContext
   */
  reloadToken() {
    return (dispatch, getState) => {
      dispatch(this.receiveLogin(
        _.merge({}, getState().security.userContext, {
          tokenCSRF: this.getCookie(TOKEN_COOKIE_NAME)
        })
      ));
    };
  }

  /**
   * Returns true, if user is logged in
   */
  static isAuthenticated(userContext = null) {
    if (!userContext) {
      return AuthenticateService.isAuthenticated();
    }
    return userContext.isAuthenticated;
  }

  /**
   * Returns true, if user is "big boss"
   */
  static isAdmin(userContext = null) {
    if (!userContext) {
      userContext = AuthenticateService.getUserContext();
    }
    return SecurityManager.hasAuthority('APP_ADMIN', userContext);
  }

  /**
   * Returns true, if given username equals authenticated username
   */
  static equalsAuthenticated(username, userContext = null) {
    if (!username) {
      return false;
    }
    if (!userContext) {
      userContext = AuthenticateService.getUserContext();
    }
    if (!SecurityManager.isAuthenticated(userContext)) {
      return false;
    }
    return username === userContext.username;
  }

  /**
   * Returns true, if user has given authority
   */
  static hasAuthority(authority, userContext = null) {
    if (!userContext) {
      userContext = AuthenticateService.getUserContext();
    }
    if (!SecurityManager.isAuthenticated(userContext) || !userContext.authorities || !authority) {
      return false;
    }
    return _.includes(userContext.authorities, authority);
  }

  /**
   * Returns true, if user has any of given authorities, false otherwise.
   *
   * @param  {arrayOf(string)}  authorities
   * @param  {userContext}  userContext
   * @return {Boolean}
   */
  static hasAnyAuthority(authorities, userContext = null) {
    if (!userContext) {
      userContext = AuthenticateService.getUserContext();
    }
    if (!SecurityManager.isAuthenticated(userContext) || !userContext.authorities || !authorities) {
      return false;
    }
    return _.intersection(userContext.authorities, authorities).length > 0;
  }

  /**
   * Returns true, if user has all of given authorities, false otherwise.
   *
   * @param  {arrayOf(string)}  authorities
   * @param  {userContext}  userContext
   * @return {Boolean}
   */
  static hasAllAuthorities(authorities, userContext = null) {
    if (!userContext) {
      userContext = AuthenticateService.getUserContext();
    }
    if (!SecurityManager.isAuthenticated(userContext) || !userContext.authorities || !authorities) {
      return false;
    }
    return _.difference(authorities, userContext.authorities).length === 0;
  }

  /**
   * Return true, if user fits in at least one access item - {@see ConfigLoader} for available access types
   */
  static hasAccess(accessItems, userContext) {
    if (!accessItems) {
      return false;
    }
    if (!accessItems || accessItems.length === 0) {
      return false;
    }

    const hasDenyAll = accessItems.some(accessItem => {
      if (accessItem.type && accessItem.type === 'DENY_ALL') {
        return true;
      }
    });
    if (hasDenyAll) {
      return false;
    }

    return accessItems.some(accessItem => {
      if (accessItem.type) {
        switch (accessItem.type) {
          case 'PERMIT_ALL': {
            return true;
          }
          case 'NOT_AUTHENTICATED': {
            return !SecurityManager.isAuthenticated(userContext);
          }
          case 'IS_AUTHENTICATED': {
            if (SecurityManager.isAuthenticated(userContext)) {
              return true;
            }
          }
          case 'HAS_ANY_AUTHORITY': {
            return SecurityManager.hasAnyAuthority(accessItem.authorities, userContext);
          }
          case 'HAS_ALL_AUTHORITIES': {
            return SecurityManager.hasAllAuthorities(accessItem.authorities, userContext);
          }
          default : {
            return null;
          }
        }
      }
    });
  }

  /**
   * Checks access from routes
   */
  static checkAccess(nextState, replace) {
    const userContext = AuthenticateService.getUserContext();
    const lastRoute = nextState.routes.slice(-1)[0];
    //
    if (!SecurityManager.hasAccess(lastRoute.access, userContext)) {
      replace({
        pathname: (SecurityManager.isAuthenticated(userContext)) ? '/error/403' : '/login',
        state: { nextPathname: nextState.location.pathname }
      });
    }
  }
}
