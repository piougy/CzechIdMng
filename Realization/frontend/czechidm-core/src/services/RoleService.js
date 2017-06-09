import FormableEntityService from './FormableEntityService';
import RestApiService from './RestApiService';
import SearchParameters from '../domain/SearchParameters';
import * as Utils from '../utils';

/**
 * Role's endpoint
 *
 * @author Radek Tomiška
 */
export default class RoleService extends FormableEntityService {

  getApiPath() {
    return '/roles';
  }

  getNiceLabel(role) {
    if (!role) {
      return '';
    }
    return role.name;
  }

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'ROLE';
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
