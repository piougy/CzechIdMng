import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import ConfigurationManager from './ConfigurationManager';
import { CacheService } from '../../services';
import DataManager from './DataManager';

/**
 * Provides information about caches from backend and their administrative methods.
 *
 * @author Peter Štrunc
 */
export default class CacheManager extends EntityManager {

  constructor() {
    super();
    this.service = new CacheService();
    this.dataManager = new DataManager();
    this.configurationManager = new ConfigurationManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Cache';
  }

  getCollectionType() {
    return 'caches';
  }

  fetchAvailableCaches() {
    const uiKey = CacheManager.UI_KEY_CACHES;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getAvailableCaches()
        .then(json => {
          let caches = new Immutable.Map();
          if (json._embedded) {
            json._embedded[this.getCollectionType()].forEach(item => {
              caches = caches.set(item.id, item);
            });
          }
          dispatch(this.dataManager.receiveData(uiKey, caches));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * Evict cache with given name
   *
   * @param {string} cacheId
   * @param {func} cb
   */
  evictCache(cacheId, cb = null) {
    if (!cacheId) {
      return null;
    }
    const uiKey = CacheManager.UI_KEY_CACHES;
    return (dispatch, getState) => {
      dispatch(this.requestEntity(cacheId, uiKey));
      this.getService().evictCache(cacheId)
        .then(json => {
          const caches = DataManager.getData(getState(), CacheManager.UI_KEY_CACHES);
          dispatch(this.dataManager.receiveData(uiKey, caches));
          if (cb) {
            cb(json, null);
          }
        })
        .catch(error => {
          dispatch(this.receiveError({ id: cacheId }, uiKey, error, cb));
        });
    };
  }


  /**
   * Evict all caches
   *
   * @param {func} cb
   */
  evictAllCaches(cb = null) {
    const uiKey = CacheManager.UI_KEY_CACHES;
    return (dispatch, getState) => {
      dispatch(this.requestEntity(uiKey));
      this.getService().evictAllCaches()
        .then(json => {
          const caches = DataManager.getData(getState(), CacheManager.UI_KEY_CACHES);
          dispatch(this.dataManager.receiveData(uiKey, caches));
          if (cb) {
            cb(json, null);
          }
        })
        .catch(error => {
          dispatch(this.receiveError({ }, uiKey, error, cb));
        });
    };
  }
}


CacheManager.UI_KEY_CACHES = 'caches';
CacheManager.CODE_MODULE_ID = 'core';
CacheManager.APP_MODULE_ID = 'app';
