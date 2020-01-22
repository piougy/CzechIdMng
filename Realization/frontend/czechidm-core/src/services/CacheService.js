import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import * as Utils from '../utils';
import SearchParameters from '../domain/SearchParameters';

export default class CacheService extends AbstractService {

  getApiPath() {
    return '/caches';
  }

  getNiceLabel(cache) {
    if (!cache) {
      return '';
    }
    return cache.name;
  }

  /**
   * Returns all installed modules
   *
   * @return Promise
   */
  getAvailableCaches() {
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
   * Evict cache with given name
   *
   * @param {string} cacheName
   */
  evictCache(cacheId) {
    console.log('');
    return RestApiService
    .patch(this.getApiPath() + `/${cacheId}/evict`)
    .then(response => {
      if (response.status === 204) {
        return {
          id: cacheId
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
