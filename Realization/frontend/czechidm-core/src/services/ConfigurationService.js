import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import SearchParameters from '../domain/SearchParameters';
import * as Utils from '../utils';
import PlainTextApi from './PlainTextApi';

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

  supportsPatch() {
    return false;
  }

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'CONFIGURATION';
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
    .get(RestApiService.getUrl(`/public${this.getApiPath()}`), '')
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
    .get(this.getApiPath() + '/all/file')
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
    .get(this.getApiPath() + '/all/environment')
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
   * Creates more configuration's items
   *
   * @param {text}
   * @return Promise
   */
  addMoreEntities(text) {
    return PlainTextApi.put(this.getApiPath() + `/bulk/save`, text)
    .then(response => {
      return response;
    })
    .then(jsonResponse => {
      if (Utils.Response.hasError(jsonResponse)) {
        throw Utils.Response.getFirstError(jsonResponse);
      }
      if (Utils.Response.hasInfo(jsonResponse)) {
        throw Utils.Response.getFirstInfo(jsonResponse);
      }
      return jsonResponse;
    });
  }
}
