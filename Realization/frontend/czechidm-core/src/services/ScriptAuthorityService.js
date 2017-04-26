import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

/**
 * Script authority service
 * (allowed class and services for script)
 */
export default class ScriptAuthorityService extends AbstractService {

  constructor() {
    super();
  }

  getApiPath() {
    return '/script-authorities';
  }

  getNiceLabel(entity) {
    if (!entity || !entity._embedded) {
      return '';
    } else if (entity.className) {
      return entity.className;
    }
    return entity.service;
  }

  // dto
  supportsPatch() {
    return false;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('service', 'asc');
  }

  getAvailableServices() {
    return RestApiService
    .get(RestApiService.getUrl(this.getApiPath() + '/search/service'))
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
