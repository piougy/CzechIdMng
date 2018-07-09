import jwtDecode from 'jwt-decode';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

const authPath = '/authentication';
const remoteAuthPath = authPath + '/remote-auth';

let lastToken = null;

/**
 * Login / logout service
 * - uses local storage to persist user context
 * - works with tokens
 * - provide FE password generation
 *
 * @author Radek Tomiška
 */
export default class AuthenticateService {

  /**
   * Return login promise
   */
  login(username, password) {
    const json = {
      username,
      password
    };
    return RestApiService
    .post(authPath, json, false) // false - we don't want to append auth token
    .then(response => {
      return response.json();
    })
    .then(this._handleOptionalErrorModel);
  }

  remoteLogin() {
    return RestApiService
      .get(remoteAuthPath)
      .then(response => response.json())
      .then(this._handleOptionalErrorModel);
  }

  _handleOptionalErrorModel(jsonResponse) {
    if (Utils.Response.hasError(jsonResponse)) {
      throw Utils.Response.getFirstError(jsonResponse);
    }
    return jsonResponse;
  }

  /**
   * Clear security storage only (~logout FE)
   */
  clearStorage() {
    delete localStorage['czechidm-storage'];
  }

  /**
   * Logout from BE
   *
   * @return Returns logout promise
   */
  logout() {
    return RestApiService
      .delete(authPath) // we want to append auth token - token will be disabled
      .then(response => {
        if (response.status === 204) { // no content - ok
          return null;
        }
        return response.json();
      })
      .then(json => {
        this.clearStorage();
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        if (Utils.Response.hasInfo(json)) {
          throw Utils.Response.getFirstInfo(json);
        }
        return json;
      });
  }

  /**
   * Returns true, if user is logged in
   */
  static isAuthenticated() {
    const userContext = AuthenticateService.getUserContext();
    if (!userContext) {
      return false;
    }
    return userContext.isAuthenticated;
  }

  /**
   * Returns logged user tokenCSRF
   * @return {string} token
   */
  static getTokenCSRF() {
    const userContext = AuthenticateService.getUserContext();
    if (!userContext || !userContext.isAuthenticated) {
      return null;
    }
    return userContext.tokenCSRF;
  }

  /**
   * Returns logged user CIDMST token
   * @return {string} token
   */
  static getTokenCIDMST() {
    const userContext = AuthenticateService.getUserContext();
    if (!userContext || !userContext.isAuthenticated) {
      return null;
    }
    return userContext.tokenCIDMST;
  }

  static getLastToken() {
    const _lt = lastToken;
    lastToken = null;
    return _lt;
  }

  /**
   * Returns logged user CIDMST token
   * @return {string} token
   */
  static setTokenCIDMST(token) {
    if (!token || token === null) {
      return;
    }
    lastToken = token;
  }

  /**
   * Returns token expiration - iso date time format
   */
  static getTokenExpiration(decodedToken) {
    return decodedToken.exp;
  }

  /**
   * Decodes JWT token as a plain JSON object.
   * @return {object} decoded token
   */
  static decodeToken(token) {
    return jwtDecode(token);
  }

  /**
   * Returns logged username
   * @return {strine} username
   */
  static getUserName() {
    const userContext = AuthenticateService.getUserContext();
    if (!userContext) {
      return null;
    }
    return userContext.username;
  }

  /**
   * Returns logged user context (username, roles etc.)
   *
   * @return {object} userContext
   */
  static getUserContext() {
    if (!AuthenticateService.getSecurityStore()) {
      return null;
    }
    return AuthenticateService.getSecurityStore().userContext;
  }

  /**
   * @return {object} redux security store from local storage
   */
  static getSecurityStore() {
    const storage = AuthenticateService.getStorage();
    if (!storage || !storage.security) {
      return null;
    }
    return storage.security;
  }

  /**
   * @return {object} redux store from local storage
   */
  static getStorage() {
    if (!localStorage['czechidm-storage']) {
      return null;
    }
    return JSON.parse(localStorage['czechidm-storage']);
  }

  /**
   * Returns requested cookie by given name from document.cookie
   *
   * @param  {string} cookiename
   * @return {string}
   */
  getCookie(cookiename) {
    // Get name followed by anything except a semicolon
    const cookiestring = RegExp('' + cookiename + '[^;]+').exec(document.cookie);
    // Return everything after the equal sign
    return unescape(!!cookiestring ? cookiestring.toString().replace(/^[^=]+./, '') : '');
  }

  /**
   * Random password, which will met password policy
   * Better way is using BE password generation - IdentityService.generatePassword();
   */
  static generatePassword(plength = 10) {
    const keylistalpha = 'abcdefghjkmnopqrstuvwx';
    const keylistint = '23456789';
    const keylistspec = '!@#_';
    let temp = '';
    let len = plength / 2;
    len = len - 1;
    const lenspec = plength - len - len;

    for (let i = 0; i < len; i++) {
      temp += keylistalpha.charAt(Math.floor(Math.random() * keylistalpha.length));
    }

    for (let i = 0; i < lenspec; i++) {
      temp += keylistspec.charAt(Math.floor(Math.random() * keylistspec.length));
    }

    for (let i = 0; i < len; i++) {
      temp += keylistint.charAt(Math.floor(Math.random() * keylistint.length));
    }
    temp = temp.split('').sort(function s() { return 0.5 - Math.random(); }).join('');
    return temp;
  }
}
