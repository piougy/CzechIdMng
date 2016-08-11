

import RestApiService from './RestApiService';
import * as Utils from '../utils';

const authPath = '/authentication';

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
    }).then(json => {
      if (Utils.Response.hasError(json)) {
        throw Utils.Response.getFirstError(json);
      }
      return json;
    });
  }

  /**
   * Logout from BE
   * @return Returns logout promise
   */
  logout() {
    delete localStorage['czechidm-storage'];
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
    if (!localStorage['czechidm-storage']) {
      return null;
    }
    const storage = JSON.parse(localStorage['czechidm-storage']);
    if (!storage || !storage.security) {
      return null;
    }
    return storage.security.userContext;
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
    temp = temp.split('').sort(function() {return 0.5 - Math.random();}).join('');
    return temp;
  }
}
