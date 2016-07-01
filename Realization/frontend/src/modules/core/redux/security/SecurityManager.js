'use strict';

import _ from 'lodash';
import { routeActions } from 'react-router-redux';
//
import { AuthenticateService, ConfigService, IdentityService, LocalizationService } from '../../services';
import FlashMessagesManager from '../flash/FlashMessagesManager';
import * as Utils from '../../utils';

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
const identityService = new IdentityService();
const configService = new ConfigService();

/**
 * Encapsulate user context / authentication and authorization (commig soon)
 */
export default class SecurityManager {

  constructor () {
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
        // resolve roles from auth
        const roles = json.authentication.authorities.map(authority => { return authority.authority });
        // construct logged user context
        const userContext = {
          username: json.username,
          isAuthenticated: true,
          tokenCIDMST: json.token,
          tokenCSRF: authenticateService.getCookie(TOKEN_COOKIE_NAME),
          roles: roles
        };
        dispatch(this.receiveLogin(userContext, redirect));
      })
      .catch(error => {
        dispatch(this.receiveLoginError(error, redirect));
      });
    }
  }

  /**
   * Load user roles
   */
  loadRoles(userContext, redirect) {
    return dispatch => {
      if (userContext && userContext.isAuthenticated) {
        identityService.getRoles(userContext.username, userContext.tokenCSRF)
        .then(json => {
          userContext.roles = json;
          dispatch(this.flashMessagesManager.removeAllMessages());
          dispatch(this.receiveLogin(userContext, redirect));
        })
        .catch(error => {
          authenticateService.logout();
          userContext.isAuthenticated = false;
          dispatch(this.flashMessagesManager.addErrorMessage({ position: 'tc' }, error));
          // we need to set usercontext, when this error happens - redirect to login page with error
          dispatch(this.receiveLogin(userContext, redirect));
          dispatch(this.receiveLoginError(error, redirect));
        });
      } else {
        dispatch(this.receiveLoginError(null, redirect));
      }
    }
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
    return (dispatch, getState) => {
      //getState().logger.debug('received login', userContext);
      // redirect after login, if needed
      if (redirect) {
        redirect(userContext.isAuthenticated);
      }
      dispatch({
        type: RECEIVE_LOGIN,
        userContext: userContext
      });
    }
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
      authenticateService.logout()
      dispatch(this.flashMessagesManager.hideAllMessages());
      dispatch(this.flashMessagesManager.addMessage({key: 'login', message: LocalizationService.i18n('content.logout.message.logout'), level: 'info', position: 'tc'}));
      dispatch(this.receiveLogout());
      if (redirect) {
        redirect();
      }
    }
  }

  /**
   * Logout without wait for promise result
   */
  logoutImmediatelly() {
    authenticateService.logout();
    return dispatch => {
      dispatch(this.receiveLogout());
    }
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
    }
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
    // TODO: move admin role name to settings

    return SecurityManager.hasRole(userContext, configService.getConfig('roles').superAdminRole);
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
   * Returns true, if user has given role
   */
  static hasRole(userContext = null, roleName) {
    if (!userContext) {
      userContext = AuthenticateService.getUserContext();
    }
    if (!SecurityManager.isAuthenticated(userContext) || !userContext.roles || !roleName) {
      return false;
    }
    return _.includes(userContext.roles, roleName);
  }

  static hasAnyRole(userContext, roleNames) {
    if (!SecurityManager.isAuthenticated(userContext) || !userContext.roles || !roleNames) {
      return false;
    }
    return _.intersection(userContext.roles, roleNames).length > 0;
  }

  /**
   * Return true, if user fits in at least one access item - @see ConfigService for available access types
   */
  static hasAccess(userContext, accessItems) {
    if (!accessItems) {
      return false;
    }
    if (!accessItems || accessItems.length === 0) {
      return false;
    }
    /*
    const hasDenyAll = accessItems.some(accessItem => {
      if (accessItem.type && accessItem.type === 'DENY_ALL') {
        return true;
      }
    });
    if (hasDenyAll) {
      return false;
    }*/
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
          case 'HAS_ANY_ROLE': {
            return SecurityManager.hasAnyRole(userContext, accessItem.roles);
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
    if (!SecurityManager.hasAccess(userContext, lastRoute.access)) {
      replace({
        pathname: (SecurityManager.isAuthenticated(userContext)) ? '/error/403' : '/login',
        state: { nextPathname: nextState.location.pathname }
      });
    }
  }
}
