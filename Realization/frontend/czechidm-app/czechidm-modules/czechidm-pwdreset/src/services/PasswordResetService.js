import {
  Services,
  Managers,
  Utils
} from 'czechidm-core';

const flashMessagesManager = new Managers.FlashMessagesManager();

/**
 * Connection to the password reset REST endpoint.
 *
 * @author Peter Sourek
 */
export default class PasswordResetService extends Services.AbstractService {

  getApiPath() {
    return '/public/password-reset';
  }

  /**
   * Registers a new user
   *
   * @param {object} form form data
   *
   * @return {Promise}
   */
  requestReset(form, callback) {
    return dispatch => {
      Services.RestApiService
      .post(this.getApiPath(), form)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          dispatch(
            flashMessagesManager.addMessage(flashMessagesManager.convertFromResultModel(Utils.Response.getFirstError(json)))
          );
        } else {
          flashMessagesManager.addMessage({ level: 'success', message: Services.LocalizationService.i18n(`pwdreset:pwdreset.successCreate`) });
        }
        return json;
      }).then(json => {
        if (callback) {
          callback(json, Utils.Response.hasError(json) ? Utils.Response.getFirstError(json) : null);
        }
      });
    };
  }

  changePasswordAfterVerification(getParams, newPassword, callback = null) {
    return dispatch => {
      Services.RestApiService
      .post(`${this.getApiPath()}/changePassword?${getParams}`, newPassword)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          dispatch(
            flashMessagesManager.addMessage(flashMessagesManager.convertFromResultModel(Utils.Response.getFirstError(json)))
          );
        } else {
          flashMessagesManager.addMessage({ level: 'success', message: Services.LocalizationService.i18n(`pwdreset:pwdreset.success`) });
        }
        return json;
      })
      .then(json => {
        if (callback) {
          callback(json, Utils.Response.hasError(json) ? Utils.Response.getFirstError(json) : null, newPassword.newPassword);
        }
      });
    };
  }
}
