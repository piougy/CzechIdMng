import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class RoleRequestService extends AbstractService {

  getApiPath() {
    return '/role-requests';
  }

  getNiceLabel(request) {
    if (!request) {
      return '';
    }
    if (request._embedded && request._embedded.identity) {
      return `${request._embedded.identity.username} (${request.state})`;
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

export default RoleRequestService;
