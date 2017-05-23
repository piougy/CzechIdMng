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
   * Returns form definitions to given entity
	 *
   * @param  {string} id entity identifier
   * @return {promise}
   */
  getFormDefinitions(id) {
    return RestApiService
      .get(this.getApiPath() + `/${encodeURIComponent(id)}/form-definitions`)
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
   * @param  {string} form definition code
   * @return {promise}
   */
  getFormValues(id, definitionCode) {
    return RestApiService
      .get(this.getApiPath() + `/${encodeURIComponent(id)}/form-values?definitionCode=${encodeURIComponent(definitionCode)}`)
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
   * @param  {string} form definition code
   * @param  {arrayOf(entity)} values filled form values
   * @return {promise}
   */
  saveFormValues(id, definitionCode, values) {
    return RestApiService
      .post(this.getApiPath() + `/${encodeURIComponent(id)}/form-values?definitionCode=${encodeURIComponent(definitionCode)}`, values)
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
