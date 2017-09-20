import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';
import { Utils } from 'czechidm-core';

/**
 * Service controlls virtual systems
 *
 * @author Vít Švanda
 */
export default class VsRequestService extends Services.AbstractService {

  getApiPath() {
    return '/vs/systems';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.name}`;
  }

  /**
   * Agenda supports authorization policies
   */
  supportsAuthorization() {
    return false;
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
  * Create virtual system
  */
  createVirtualSystem(detail) {
    return Services.RestApiService
      .post(this.getApiPath() + `/`, detail)
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
