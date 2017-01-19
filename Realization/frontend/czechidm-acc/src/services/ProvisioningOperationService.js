import { Services } from 'czechidm-core';
import { Domain, Utils } from 'czechidm-core';
import SystemEntityTypeEnum from '../domain/SystemEntityTypeEnum';

export default class ProvisioningOperationService extends Services.AbstractService {

  constructor() {
    super();
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.system ? entity.system.name : entity._embedded.system.name}:${SystemEntityTypeEnum.getNiceLabel(entity.entityType)}:${entity.systemEntityUid}`;
  }

  getApiPath() {
    return '/provisioning-operations';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('created', 'ASC');
  }

  /**
   * Retry or cancel provisioning operation
   *
   * @param  {string} operation id
   * @param  {string} action 'retry' or 'cancel'
   * @return {Promise}
   */
  retry(id, action = 'retry') {
    return Services.RestApiService
      .put(this.getApiPath() + `/${id}/${action}`)
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

  /**
   * Retry or cancel provisioning batch
   *
   * @param  {string} batch id
   * @param  {string} action 'retry' or 'cancel'
   * @return {Promise}
   */
  retryBatch(id, action = 'retry') {
    return Services.RestApiService
      .put(Services.RestApiService.getUrl(`/provisioning-batches/${id}/${action}`))
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
