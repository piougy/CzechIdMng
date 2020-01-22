import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import ConfigurationManager from './ConfigurationManager';
import { CacheService } from '../../services';
import DataManager from './DataManager';
import { backendConfigurationInit } from '../config/actions';

/**
 * Provides informations  about modules from backend and their administrative methods.
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
    const uiKey = CacheManager.UI_KEY_MODULES;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getAvailableCaches()
        .then(json => {
          let caches = new Immutable.Map();
          console.log("json ", json, json['_embedded'])
          json._embedded[this.getCollectionType()].forEach(item => {
            caches = caches.set(item.id, item);
          });
          dispatch(this.dataManager.receiveData(uiKey, caches));
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * Evict cache with given name
   *
   * @param {string} moduleId
   * @param {boolean} enable
   * @param {func} cb
   */
  evictCache(cacheId, cb = null) {
    if (!cacheId) {
      return null;
    }
    const uiKey = CacheManager.UI_KEY_MODULES;
    return (dispatch, getState) => {
      dispatch(this.requestEntity(cacheId, uiKey));
      this.getService().evictCache(cacheId)
        .then(json => {
          const caches = DataManager.getData(getState(), CacheManager.UI_KEY_MODULES);
          if (caches.has(json.id)) {
            //caches.get(json.id).size = json.size;
          }
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
}

CacheManager.UI_KEY_MODULES = 'cache';
CacheManager.CODE_MODULE_ID = 'core';
CacheManager.APP_MODULE_ID = 'app';
