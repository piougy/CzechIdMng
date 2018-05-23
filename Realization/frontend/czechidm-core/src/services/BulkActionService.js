import RestApiService from './RestApiService';
import * as Utils from '../utils';

export default class BulkActionService {

  constructor(apiPath) {
    this.apiPath = apiPath;
  }

  getApiPath() {
    return this.apiPath;
  }

  processBulkAction(action, cb) {
    return RestApiService
      .post(this.getApiPath() + `/bulk/action`, action)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        if (cb) {
          cb(json);
        }
        return json;
      });
  }

  /**
   * Returns all bulk actions in given api path
   *
   * @return Promise
   */
  getAvailableBulkActions() {
    return RestApiService
      .get(this.getApiPath() + `/bulk/action`)
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

BulkActionService.UI_KEY_PREFIX = 'bulk-actions-';
