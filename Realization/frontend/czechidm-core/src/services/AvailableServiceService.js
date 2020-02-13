import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

/**
 * Provides requests to BE for service agenda of all services 
 *  available in scripts
 *
 * @author Ondrej Husnik
 */
export default class AvailableServiceService extends AbstractService {

  getApiPath() {
    return '/available-service';
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
  getAvailableServices() {
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
}
