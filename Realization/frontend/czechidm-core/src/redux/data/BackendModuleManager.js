import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import ConfigurationManager from './ConfigurationManager';
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
    this.configurationManager = new ConfigurationManager();
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

  setEnabled(moduleId, enable = true, cb = null) {
    if (!moduleId) {
      return null;
    }
    const uiKey = BackendModuleManager.UI_KEY_MODULES;
    const entity = { id: moduleId, disabled: !enable };
    return (dispatch, getState) => {
      dispatch(this.requestEntity(moduleId, uiKey));
      this.getService().patchById(moduleId, entity)
      .then(json => {
        let installedModules = DataManager.getData(getState(), BackendModuleManager.UI_KEY_MODULES);
        installedModules = installedModules.set(json.id, json);
        dispatch(this.configurationManager.fetchPublicConfigurations());
        dispatch(this.dataManager.receiveData(uiKey, installedModules));
      })
      .catch(error => {
        dispatch(this.receiveError(entity, uiKey, error, cb));
      });
    };
  }
}

BackendModuleManager.UI_KEY_MODULES = 'installed-modules';
BackendModuleManager.CODE_MODULE_ID = 'core';
BackendModuleManager.APP_MODULE_ID = 'app';
