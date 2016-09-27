import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import { BackendModuleService } from '../../services';
import DataManager from './DataManager';

/**
 * Provides informations  about modules from backend and their administrative methods.
 */
export default class BackendModuleManager extends EntityManager {

  constructor() {
    super();
    this.service = new BackendModuleService();
    this.dataManager = new DataManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Module';
  }

  fetchInstalledModules() {
    const uiKey = BackendModuleManager.UI_KEY_MODULES;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getInstalledModules()
        .then(json => {
          let installedModules = new Immutable.Map();
          json.forEach(item => {
            installedModules = installedModules.set(item.id, item);
          });
          dispatch(this.dataManager.receiveData(uiKey, installedModules));
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * Returns setting value
   */
  static isEnabled(state, moduleId) {
    const installedModules = DataManager.getData(state, BackendModuleManager.UI_KEY_MODULES);
    if (!installedModules) {
      return false;
    }
    if (!installedModules.has(moduleId)) {
      return false;
    }
    return !installedModules.get(moduleId).disabled;
  }
}

BackendModuleManager.UI_KEY_MODULES = 'installed-modules';
BackendModuleManager.CODE_MODULE_ID = 'core';
BackendModuleManager.APP_MODULE_ID = 'app';
