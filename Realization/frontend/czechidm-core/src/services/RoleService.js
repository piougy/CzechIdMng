import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import SearchParameters from '../domain/SearchParameters';
import * as Utils from '../utils';

class RoleService extends AbstractService {

  getApiPath() {
    return '/roles';
  }

  getNiceLabel(role) {
    if (!role) {
      return '';
    }
    return role.name;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }

  getAvailableAuthorities() {
    return RestApiService
    .get(RestApiService.getUrl('/authorities/search/available'))
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

export default RoleService;
