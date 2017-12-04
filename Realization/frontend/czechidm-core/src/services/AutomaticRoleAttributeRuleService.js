import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import AutomaticRoleAttributeService from './AutomaticRoleAttributeService';

/**
 * Rules for automatic role service
 *
 * @author Ondrej Kopr
 */
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
}
