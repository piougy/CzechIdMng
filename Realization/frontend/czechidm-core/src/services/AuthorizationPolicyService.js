import AbstractRequestService from './AbstractRequestService';
import SearchParameters from '../domain/SearchParameters';
import RoleService from './RoleService';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

/**
 * Role granted authorities.
 *
 * @author Radek Tomiška
 */
export default class AuthorizationPolicyService extends AbstractRequestService {

  constructor() {
    super();
    this.roleService = new RoleService();
  }

  getSubApiPath() {
    return '/authorization-policies';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    if (!entity._embedded) {
      return entity.id;
    }
    //
    if (!entity._embedded.role && !entity.authorizableType) {
      return `${ Utils.Ui.getSimpleJavaType(entity.evaluatorType) }`;
    }

    if (!entity._embedded.role && entity.authorizableType) {
      return `${ Utils.Ui.getSimpleJavaType(entity.authorizableType) } - ${ Utils.Ui.getSimpleJavaType(entity.evaluatorType) }`;
    }
    return `${ this.roleService.getNiceLabel(entity._embedded.role) } - ${ Utils.Ui.getSimpleJavaType(entity.evaluatorType) }`;
  }

  supportsPatch() {
    return false;
  }

  supportsBulkAction() {
    return true;
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
    return super.getDefaultSearchParameters()
      .setName(SearchParameters.NAME_QUICK)
      .clearSort()
      .setSort('seq')
      .setSort('groupPermission')
      .setSort('authorizableType')
      .setSort('evaluatorType')
      .setSize(50);
  }

  /**
   * Loads all registered evaluators (available for authorization policies)
   *
   * @return {promise}
   */
  getSupportedEvaluators() {
    return RestApiService
      .get(`${ this.getApiPath() }/search/supported`)
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
      .get(`${ this.getApiPath() }/search/authorizable-types`)
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
