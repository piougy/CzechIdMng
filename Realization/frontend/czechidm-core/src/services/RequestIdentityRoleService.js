import FormableEntityService from './FormableEntityService';
import SearchParameters from '../domain/SearchParameters';

class RequestIdentityRoleService extends FormableEntityService {

  getApiPath() {
    return '/request-identity-roles';
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
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('role.name', 'desc');
  }
}

export default RequestIdentityRoleService;
