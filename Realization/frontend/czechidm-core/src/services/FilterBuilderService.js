import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

/**
 * Provides information about filter builders from backend and their administrative methods.
 *
 * @author Kolychev Artem
 * @author Radek Tomiška
 * @since 9.7.7
 */
export default class FilterBuilderService extends AbstractService {

  getApiPath() {
    return '/filter-builders';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.id;
  }

  /**
    * Returns all registered filters
    *
    * @return Promise
    */
  getRegisteredFilterBuilders() {
    return RestApiService
      .get(this.getApiPath())
      .then(response => response.json())
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Activate filter.
   *
   * @param {string} filterBuilderId
   */
  setEnabled(filterBuilderId) {
    return RestApiService
      .patch(`${ this.getApiPath() }/${ encodeURIComponent(filterBuilderId) }/enable`)
      .then(response => {
        if (response.status === 204) {
          return null;
        }
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
