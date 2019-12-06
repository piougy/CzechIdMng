import _ from 'lodash';
//
import { Services, Domain, Utils } from 'czechidm-core';
import SystemEntityTypeEnum from '../domain/SystemEntityTypeEnum';

/**
 * Active provisioning operations in the queue.
 *
 * @author Radek Tomiška
 */
export default class ProvisioningOperationService extends Services.AbstractService {

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${(entity._embedded && entity._embedded.system) ? entity._embedded.system.name : entity.system}:${SystemEntityTypeEnum.getNiceLabel(entity.entityType)}:${entity.systemEntityUid}`;
  }

  getApiPath() {
    return '/provisioning-operations';
  }

  getGroupPermission() {
    return 'PROVISIONINGOPERATION';
  }

  supportsBulkAction() {
    return true;
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('created', 'DESC');
  }

  /**
   * Delete all provisioning operation from queue for the given system identifier.
   * If system identifier will be null. Delete all provisioning operation for all
   * system.
   *
   * @param  {string} system identifier
   * @return {Promise}
   */
  deleteAll(systemId = null) {
    let deleteUrl = '/action/bulk/delete';
    if (!_.isNil(systemId)) {
      deleteUrl = `${ deleteUrl }?system=${ encodeURIComponent(systemId) }`;
    }
    return Services.RestApiService
      .delete(Services.RestApiService.getUrl(this.getApiPath() + deleteUrl))
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        if (response.status === 204) {
          return {};
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }
}
