import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import ResponseUtils from '../utils/ResponseUtils';

/**
 * Validates ReCaptcha requests.
 *
 * @author Filip Mestanek
 */
export default class RecaptchaService extends AbstractService {

  getApiPath() {
    return '/public/recaptcha';
  }

  /**
   * Checks recaptcha response from Google. See https://developers.google.com/recaptcha/docs/verify for API.
   *
   * @param {string} response response from the recaptcha
   *
   * @return {Promise}
   */
  checkResponse(response) {
    const payload = {
      response
    };

    return RestApiService
      .post(this.getApiPath(), payload)
      .then(serviceResponse => {
        return serviceResponse.json();
      })
      .then(json => {
        if (ResponseUtils.hasError(json)) {
          throw ResponseUtils.getFirstError(json);
        }

        return json;
      });
  }
}
