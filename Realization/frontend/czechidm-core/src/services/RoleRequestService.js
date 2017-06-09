import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import ResponseUtils from '../utils/ResponseUtils';

class RoleRequestService extends AbstractService {

  getApiPath() {
    return '/role-requests';
  }

  getNiceLabel(request) {
    if (!request) {
      return '';
    }
    if (request._embedded && request._embedded.applicant) {
      return `${request._embedded.applicant.username} (${request.state})`;
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

  startRequest(idRequest) {
    return RestApiService.put(this.getApiPath() + `/${idRequest}/start`, null).then(response => {
      if (response.status === 403) {
        throw new Error(403);
      }
      if (response.status === 404) {
        throw new Error(404);
      }
      return response.json();
    })
    .then(json => {
      if (ResponseUtils.hasError(json)) {
        throw ResponseUtils.getFirstError(json);
      }
      return json;
    });
  }

}

export default RoleRequestService;
