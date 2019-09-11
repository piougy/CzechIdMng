import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

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

}
