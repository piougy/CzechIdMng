import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import SearchParameters from '../domain/SearchParameters';
import * as Utils from '../utils';

export default class ConfigurationService extends AbstractService {

  getApiPath() {
    return '/configurations';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name', 'asc');
  }

  /**
   * Returns all public configurations
   *
   * @return Promise
   */
  getPublicConfigurations() {
    return RestApiService
    .get(RestApiService.getUrl('/public/configurations'))
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
   * Returns all configurations from property files
   *
   * @return Promise
   */
  getAllConfigurationsFromFile() {
    return RestApiService
    .get(this.getApiPath() + '/file')
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
   * Returns all configurations from property files
   *
   * @return Promise
   */
  getAllConfigurationsFromEnvironment() {
    return RestApiService
    .get(this.getApiPath() + '/environment')
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
