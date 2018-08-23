import AbstractRequestFormableService from './AbstractRequestFormableService';
import RestApiService from './RestApiService';
import SearchParameters from '../domain/SearchParameters';
import * as Utils from '../utils';

/**
 * Role's endpoint
 *
 * @author Radek Tomiška
 */
export default class RoleService extends AbstractRequestFormableService {

  /**
   * Using in the request
   */
  getSubApiPath() {
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

  supportsBulkAction() {
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

  /**
   * Returns authorities from all enabled modules.
   *
   * @return {promise}
   */
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

  /**
   * Returns authorities from all instaled modules. All authorities are needed in security cofiguration. Module can be disabled, but configured security has to remain.
   *
   * @return {promise}
   */
  getAllAuthorities() {
    return RestApiService
    .get(RestApiService.getUrl('/authorities/search/all'))
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
