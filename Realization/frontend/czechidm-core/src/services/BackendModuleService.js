import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

export default class BackendModuleService extends AbstractService {

  getApiPath() {
    return '/modules';
  }

  getNiceLabel(backendModule) {
    if (!backendModule) {
      return '';
    }
    return backendModule.name;
  }

  /**
   * Returns all installed modules
   *
   * @return Promise
   */
  getInstalledModules() {
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
   * Set module endabled / disabled.
   * Supports BE and FE modules
   *
   * @param {string} moduleId
   * @param {boolean} enable
   */
  setEnabled(moduleId, enable = true) {
    return RestApiService
    .patch(this.getApiPath() + `/${moduleId}/${enable ? 'enable' : 'disable'}`)
    .then(response => {
      if (response.status === 204) {
        // construct basic information about de/activated module
        return {
          id: moduleId,
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

  getResultCodes(moduleId) {
    return RestApiService
      .get(this.getApiPath() + `/${moduleId}/result-codes`)
      .then(response => {
        if (response.status === 204) {
          return {
            id: moduleId
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
