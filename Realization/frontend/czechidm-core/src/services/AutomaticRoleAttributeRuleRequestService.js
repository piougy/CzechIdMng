import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class AutomaticRoleAttributeRuleRequestService extends AbstractService {

  getApiPath() {
    return '/automatic-role-rule-requests';
  }

  getNiceLabel(request) {
    if (!request) {
      return '';
    }
    if (request.attributeName) {
      return `${request.attributeName}`;
    }
    return request.id;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }
}

export default AutomaticRoleAttributeRuleRequestService;
