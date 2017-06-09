import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class ConceptRoleRequestService extends AbstractService {

  getApiPath() {
    return '/concept-role-requests';
  }

  getNiceLabel(request) {
    if (!request) {
      return '';
    }
    if (request._embedded && request._embedded.role) {
      return `${request._embedded.role.name}`;
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

export default ConceptRoleRequestService;
