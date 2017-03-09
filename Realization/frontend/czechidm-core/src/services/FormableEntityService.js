import * as Utils from '../utils';
import AbstractService from './AbstractService';
import RestApiService from './RestApiService';

/**
 * Service for entity with eav attributes endpoint
 *
 * @author Radek Tomiška
 */
export default class FormableEntityService extends AbstractService {
  /**
   * Returns form definition to given entity
	 *
   * @param  {string} id entity identifier
   * @return {promise}
   */
  getFormDefinition(id) {
    return RestApiService
      .get(this.getApiPath() + `/${encodeURIComponent(id)}/form-definition`)
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
   * Returns filled form values
	 *
   * @param  {string} id entity identifier
   * @return {promise}
   */
  getFormValues(id) {
    return RestApiService
      .get(this.getApiPath() + `/${encodeURIComponent(id)}/form-values`)
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
   * Saves form values
   *
   * @param  {string} id identity identifier
   * @param  {arrayOf(entity)} values filled form values
   * @return {promise}
   */
  saveFormValues(id, values) {
    return RestApiService
      .post(this.getApiPath() + `/${encodeURIComponent(id)}/form-values`, values)
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
