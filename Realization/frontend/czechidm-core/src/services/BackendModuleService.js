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
}
