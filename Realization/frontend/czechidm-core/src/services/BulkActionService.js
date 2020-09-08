import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

/**
 * Provides information about bulk actions from backend and their administrative methods.
 *
 * @author Radek Tomiška
 * @since 10.6.0
 */
export default class BulkActionService extends AbstractService {

  getApiPath() {
    return '/bulk-actions';
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
  getRegisteredBulkActions() {
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
   * Set bulk action endabled / disabled.
   *
   * @param {string} bulkActionId
   * @param {boolean} enable
   */
  setEnabled(bulkActionId, enable = true) {
    return RestApiService
      .patch(`${ this.getApiPath() }/${ encodeURIComponent(bulkActionId) }/${ enable ? 'enable' : 'disable' }`)
      .then(response => {
        if (response.status === 204) {
          // construct basic information about de/activated module
          return {
            id: bulkActionId,
            disabled: !enable
          };
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
