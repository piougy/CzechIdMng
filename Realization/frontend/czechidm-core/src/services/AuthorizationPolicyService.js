import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RoleService from './RoleService';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

/**
 * Role's granted authorities
 *
 * @author Radek Tomiška
 */
export default class AuthorizationPolicyService extends AbstractService {

  constructor() {
    super();
    this.roleService = new RoleService();
  }

  getApiPath() {
    return '/authorization-policies';
  }

  getNiceLabel(entity) {
    if (!entity || !entity._embedded) {
      return '';
    }
    return `${this.roleService.getNiceLabel(entity._embedded.role)} - ${Utils.Ui.getSimpleJavaType(entity.evaluatorType)}`;
  }

  supportsPatch() {
    return false;
  }

  getGroupPermission() {
    return 'AUTHORIZATIONPOLICY';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('seq', 'asc').setSort('evaluatorType');
  }

  /**
   * Loads all registered evaluators (available for authorization policies)
   *
   * @return {promise}
   */
  getSupportedEvaluators() {
    return RestApiService
    .get(this.getApiPath() + '/search/supported')
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
   * Loads all registered evaluators (available for authorization policies)
   *
   * @return {promise}
   */
  getAuthorizableTypes() {
    return RestApiService
    .get(this.getApiPath() + '/search/authorizable-types')
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
