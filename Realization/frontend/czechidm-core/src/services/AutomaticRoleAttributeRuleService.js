import RestApiService from './RestApiService';
import SearchParameters from '../domain/SearchParameters';
import * as Utils from '../utils';
import AbstractService from './AbstractService';
import AutomaticRoleAttributeService from './AutomaticRoleAttributeService';

/**
 * Rules for automatic role service
 *
 * @author Ondrej Kopr
 */

const REACALCULATE_PATH = '/recalculate';

export default class AutomaticRoleAttributeRuleService extends AbstractService {

  constructor() {
    super();
    this.automaticRoleAttributeService = new AutomaticRoleAttributeService();
  }

  getApiPath() {
    return '/automatic-role-attribute-rules';
  }

  getNiceLabel(entity) {
    if (!entity || !entity._embedded) {
      return '';
    }
    return `${entity.type} - ${this.automaticRoleAttributeService.getNiceLabel(entity._embedded.automaticRoleAttribute)}`;
  }

  supportsPatch() {
    return false;
  }

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'AUTOMATICROLEATTRIBUTERULE';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'asc');
  }

  /**
   * Create method with recalculate is same as basic create,
   * but after save is recalculate all identities and
   * ther automatic roles by attribute.
   */
  createAndRecalculate(json) {
    return RestApiService
      .post(this.getApiPath() + REACALCULATE_PATH, json)
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

  updateByIdAndRecalculate(id, json) {
    return RestApiService
      .put(this.getApiPath() + `/${encodeURIComponent(id)}${REACALCULATE_PATH}`, json)
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
