import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import ResponseUtils from '../utils/ResponseUtils';

class RequestItemService extends AbstractService {

  getApiPath() {
    return '/request-items';
  }

  getNiceLabel(request) {
    if (!request) {
      return '';
    }
    if (request._embedded && request._embedded.role) {
      return `${request._embedded.role.name} (${request.state})`;
    }
    return request.id;
  }

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'REQUESTITEM';
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

  /**
   * Get changes for given request item
   * @param  id Id of request item
   */
  getChanges(id) {
    return RestApiService
      .get(this.getApiPath() + `/${encodeURIComponent(id)}/changes`)
      .then(response => {
        return response.json();
      })
      .then(jsonResponse => {
        if (ResponseUtils.hasError(jsonResponse)) {
          throw ResponseUtils.getFirstError(jsonResponse);
        }
        if (ResponseUtils.hasInfo(jsonResponse)) {
          throw ResponseUtils.getFirstInfo(jsonResponse);
        }
        return jsonResponse;
      });
  }

}

export default RequestItemService;
