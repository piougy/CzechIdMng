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

  /**
   * Extended nice label
   *
   * @param  {entity} entity
   * @param  {boolean} showEnvironment environment will be rendered.
   * @return {string}
   */
  getNiceLabel(role, showEnvironment = true, showCode = true) {
    if (!role) {
      return '';
    }
    let code = null;
    if (role.name !== role.baseCode && showCode) {
      code = role.baseCode;
    }
    if (role.environment && showEnvironment) {
      if (code) {
        code = role.code;
      } else {
        code = role.environment;
      }
    }
    if (!code) {
      return role.name;
    }
    return `${role.name} (${code})`;
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

  supportsAttachment() {
    return true;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name').setSort('environment');
  }

  /**
   * Returns search parameters for search roots
   */
  getRootSearchParameters() {
    // root search - all roles can be roots
    return this.getDefaultSearchParameters().setName(SearchParameters.NAME_AUTOCOMPLETE).clearSort().setSort('childrenCount', 'desc').setSort('name').setSort('environment').setSize(50);
  }

  /**
   * Search sub roles by parent id
   */
  getTreeSearchParameters() {
    // quick search suppors filtering by parent already
    return this.getRootSearchParameters();
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

  /**
   * Returns form definition for role attributes
   *
   * @param  {string} id entity identifier
   * @return {promise}
   */
  getAttributeFormDefinition(id) {
    return RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/attribute-form-definition`)
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
   * Incompatible roles are resolved from sub roles.
   *
   * @param id {string}
   * @param token {string}
   * @return {Promise}
   */
  getIncompatibleRoles(id, token = null) {
    return RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/incompatible-roles`, token)
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
