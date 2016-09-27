import _ from 'lodash';
import { routeActions } from 'react-router-redux';
// api
import { LocalizationService, AuthenticateService } from '../../services';
// import SecurityManager from '../Security/SecurityManager';
// import SettingManager from '../data/SettingManager';

/*
 * action types
 */

export const ADD_MESSAGE = 'ADD_MESSAGE';
export const HIDE_MESSAGE = 'HIDE_MESSAGE';
export const HIDE_ALL_MESSAGES = 'HIDE_ALL_MESSAGES';
export const REMOVE_MESSAGE = 'REMOVE_MESSAGE';
export const REMOVE_ALL_MESSAGES = 'REMOVE_ALL_MESSAGES';
//
export const DEFAULT_SERVER_UNAVAILABLE_TIMEOUT = 3000;

export default class FlashMessagesManager {

  constructor() {
    this.serverUnavailableTimeout = DEFAULT_SERVER_UNAVAILABLE_TIMEOUT;
  }

  getServerUnavailableTimeout() {
    return this.serverUnavailableTimeout;
  }

  setServerUnavailableTimeout(serverUnavailableTimeout) {
    this.serverUnavailableTimeout = serverUnavailableTimeout;
  }

  addMessage(message) {
    return dispatch => {
      if (message.key) {
        dispatch(this.hideMessage(message.key));
      }
      dispatch({
        type: ADD_MESSAGE,
        message: _.merge({}, message, { date: new Date() })
      });
    };
  }

  addError(error, context = null) {
    return this.addErrorMessage({}, error, context);
  }

  addErrorMessage(message, error) {
    return (dispatch, getState) => {
      if (DEBUG) {
        getState().logger.error(message, error);
      }
      let errorMessage;
      if (error.statusEnum) { // our error message
        // automatic localization
        const messageTitle = LocalizationService.i18n(error.module + ':error.' + error.statusEnum + '.title', this._prepareParams(error.parameters, `${error.statusEnum} (${error.statusCode}:${error.id})`));
        let defaultMessage = error.message;
        if (!_.isEmpty(error.parameters)) {
          defaultMessage += ' (';
          let first = true;
          for (const parameterKey in error.parameters) {
            if (!first) {
              defaultMessage += ', ';
            } else {
              first = false;
            }
            defaultMessage += `${parameterKey}:${error.parameters[parameterKey]}`;
          }
          defaultMessage += ')';
        }
        const messageText = LocalizationService.i18n(error.module + ':error.' + error.statusEnum + '.message', this._prepareParams(error.parameters, defaultMessage));
        //
        errorMessage = _.merge({}, {
          key: error.statusEnum,
          level: (parseInt(error.statusCode, 10) < 500 ? 'warning' : 'error'), // 4xx - warning message, 5xx - error message
          title: messageTitle,
          message: messageText,
        }, message);
      } else { // system error - only contain message
        errorMessage = _.merge({}, {
          level: 'error',
          message: error.message
        }, message);
      }
      // error redirect handler
      if (this._isServerUnavailableError(error)) {
        // timeout to prevent showing message on ajax call interuption
        setTimeout(
          () => {
            dispatch(this.addUnavailableMessage());
            dispatch(this._clearConfiguations());
          }
          , this.getServerUnavailableTimeout());
      // TODO: module defined exception handlers
      } else if (this._isLoginError(error)) {
        this._logoutImmediatelly(); // we dont want to propagate LOGOUT dispatch event ... we want propagate new event:
        dispatch({
          type: 'RECEIVE_LOGIN_EXPIRED'
        });
      } else if (this._isPasswordChangeError(error)) {
        dispatch(this._logoutImmediatelly());
        const username = error.parameters.identity;
        dispatch(routeActions.push(`/password/change?name=${username}`));
      } else {
        dispatch(this.addMessage(errorMessage));
      }
    };
  }

  addUnavailableMessage() {
    return dispatch => {
      dispatch(this.addMessage({
        key: 'error-app-load',
        level: 'error',
        title: LocalizationService.i18n('content.error.503.description'),
        message: LocalizationService.i18n('content.error.503.note'),
        dismissible: false/* ,
        action: {
          label: 'Odeslat na podporu',
          callback: () => {
            alert('Comming soon.')
          }
        }*/
      }));
    };
  }

  // TODO: cyclic dependency in security manager
  _logoutImmediatelly() {
    const authenticateService = new AuthenticateService();
    authenticateService.logout();
    return {
      type: 'LOGOUT'
    };
  }

  // TODO: cyclic dependency in configuration manager - refactor action constants to separate file
  _clearConfiguations() {
    return {
      type: 'APP_UNAVAILABLE'
    };
  }

  /**
   * Returns true, if new login is needed
   */
  _isLoginError(error) {
    if (!error.statusEnum) {
      return false;
    }
    if (error.statusEnum === 'XSRF' || error.statusEnum === 'LOG_IN' || error.statusEnum === 'AUTH_EXPIRED') {
      return true;
    }
    return false;
  }

  /**
   * Refurn true, if pasword has to be changed before usage of application
   */
  _isPasswordChangeError(error) {
    if (!error.statusEnum) {
      return false;
    }
    if (error.statusEnum === 'MUST_CHANGE_IDM_PASSWORD') {
      return true;
    }
    return false;
  }

  /**
   * Returns true, if BE is not available
   */
  _isServerUnavailableError(error) {
    if (error.message && (error.message.indexOf('NetworkError') > -1 || error.message.indexOf('Failed to fetch') > -1 || (error.statusCode && (error.statusCode === '400' || error.statusCode === '503')))) {
      return true;
    }
    return false;
  }

  _prepareParams(params, defaultMessage) {
    let results = {};
    if (params) {
      results = _.merge({}, params);
    }
    if (defaultMessage) {
      results.defaultValue = defaultMessage;
    }
    return results;
  }

  hideMessage(id) {
    return {
      type: HIDE_MESSAGE,
      id
    };
  }

  removeMessage(id) {
    return {
      type: REMOVE_MESSAGE,
      id
    };
  }

  hideAllMessages() {
    return {
      type: HIDE_ALL_MESSAGES
    };
  }

  removeAllMessages() {
    return {
      type: REMOVE_ALL_MESSAGES
    };
  }

}
