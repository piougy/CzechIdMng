import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

export default class EntityEventProcessorService extends AbstractService {

  getApiPath() {
    return '/entity-event-processors';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.id;
  }

  /**
   * Returns all registered processors
   *
   * @return Promise
   */
  getReqisteredProcessors() {
    return RestApiService
    .get(this.getApiPath())
    .then(response => {
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
   * Set processor endabled / disabled.
   *
   * @param {string} processorId
   * @param {boolean} enable
   */
  setEnabled(processorId, enable = true) {
    return RestApiService
    .patch(this.getApiPath() + `/${processorId}/${enable ? 'enable' : 'disable'}`)
    .then(response => {
      if (response.status === 204) {
        // construct basic information about de/activated module
        return {
          id: processorId,
          disabled: !enable
        };
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
