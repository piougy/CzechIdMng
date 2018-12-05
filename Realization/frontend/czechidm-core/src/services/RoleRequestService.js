import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import ResponseUtils from '../utils/ResponseUtils';

import * as Utils from '../utils';

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

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'ROLEREQUEST';
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

  copyRolesByIdentity(roleRequestByIdentity) {
    const roleRequestId = roleRequestByIdentity.roleRequest;
    return RestApiService
      .post(this.getApiPath() + `/${encodeURIComponent(roleRequestId)}/copy-roles`, roleRequestByIdentity)
      .then(response => {
        return response.json();
      })
      .then(jsonResponse => {
        if (Utils.Response.hasError(jsonResponse)) {
          throw Utils.Response.getFirstError(jsonResponse);
        }
        return jsonResponse;
      }).catch(ex => {
        throw this._resolveException(ex);
      });
  }

}

export default RoleRequestService;
