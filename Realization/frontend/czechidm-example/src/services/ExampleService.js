import { Services } from 'czechidm-core';
import { Utils } from 'czechidm-core';

/**
 * Example service:
 * - example rest methods
 *
 * @author Radek Tomiška
 */
export default class ExampleProductService {

  /**
   * Client error example
   *
   * @param  {string} some value
   * @return {Promise}
   */
  clientError(parameter = 'Test') {
    return Services.RestApiService
      .get(`/examples/client-error?parameter=${encodeURIComponent(parameter)}`)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Server error example
   *
   * @param  {string} some value
   * @return {Promise}
   */
  serverError(parameter = 'Test') {
    return Services.RestApiService
      .get(`/examples/server-error?parameter=${encodeURIComponent(parameter)}`)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }
}
