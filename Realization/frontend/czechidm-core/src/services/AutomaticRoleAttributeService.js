import RestApiService from './RestApiService';
import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RoleService from './RoleService';
import * as Utils from '../utils';

/**
 * Automatic roles service
 *
 * @author Ondrej Kopr
 */

const REACALCULATE_PATH = '/recalculate';

export default class AutomaticRoleAttributeService extends AbstractService {

  constructor() {
    super();
    this.roleService = new RoleService();
  }

  getApiPath() {
    return '/automatic-role-attributes';
  }

  getNiceLabel(entity) {
    if (!entity || !entity._embedded) {
      return '';
    }
    return `${entity.name} - ${this.roleService.getNiceLabel(entity._embedded.role)}`;
  }

  supportsPatch() {
    return false;
  }

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'AUTOMATICROLEATTRIBUTE';
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
   * Recalculate given automatic role by attribute
   */
  recalculate(id, callback = null) {
    return RestApiService
      .post(this.getApiPath() + '/' + id + REACALCULATE_PATH)
      .then(jsonResponse => {
        if (Utils.Response.hasError(jsonResponse)) {
          throw Utils.Response.getFirstError(jsonResponse);
        }
        if (Utils.Response.hasInfo(jsonResponse)) {
          throw Utils.Response.getFirstInfo(jsonResponse);
        }
        return jsonResponse.json();
      })
      .then(json => {
        if (callback !== null) {
          callback(json);
        }
        return json;
      });
  }

  /**
   * Delete automatic role via request
   * @param  {[type]} id [description]
   * @return {[type]}    [description]
   */
  deleteAutomaticRolesViaRequest(id) {
    return RestApiService
      .delete(this.getApiPath() + `/delete-via-request/${encodeURIComponent(id)}`)
      .then(response => {
        return response.json();
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
