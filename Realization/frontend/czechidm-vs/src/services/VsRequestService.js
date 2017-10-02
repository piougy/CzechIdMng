import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';
import { Utils } from 'czechidm-core';

/**
 * Service controlls request for virtual systems
 *
 * @author Vít Švanda
 */
export default class VsRequestService extends Services.AbstractService {

  getApiPath() {
    return '/vs/requests';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.uid}`;
  }

  /**
   * Agenda supports authorization policies
   */
  supportsAuthorization() {
    return true;
  }

  /**
   * Group permission - all base permissions (`READ`, `WRITE`, ...) will be evaluated under this group
   */
  getGroupPermission() {
    return 'VSREQUEST';
  }

  /**
   * Almost all dtos doesn§t support rest `patch` method
   */
  supportsPatch() {
    return false;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }

  /**
  * Mark virtual system request as realized (changes will be propagated to VsAccount)
  */
  realize(id) {
    return Services.RestApiService
      .put(this.getApiPath() + `/${encodeURIComponent(id)}/realize`)
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

  /**
  * Cancel virtual system request
  */
  cancel(id, reason) {
    return Services.RestApiService
      .put(this.getApiPath() + `/${encodeURIComponent(id)}/cancel`, {reason})
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

  /**
  * Get connector object by given virtual system request. Call directly connector.
  */
  getConnectorObject(id) {
    return Services.RestApiService
      .get(this.getApiPath() + `/${encodeURIComponent(id)}/connector-object`)
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
  /**
  * Get connector object by given virtual system request.
  * Show wish for this request = current VS account attributes + changes from request.
  */
  getWishConnectorObject(id) {
    return Services.RestApiService
      .get(this.getApiPath() + `/${encodeURIComponent(id)}/wish-connector-object`)
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
