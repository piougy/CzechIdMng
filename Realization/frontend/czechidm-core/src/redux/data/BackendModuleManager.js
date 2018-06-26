import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import ConfigurationManager from './ConfigurationManager';
import { BackendModuleService } from '../../services';
import DataManager from './DataManager';
import { backendConfigurationInit } from '../config/actions';

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

  /**
   * Set module endabled / disabled.
   * Supports BE and FE modules
   *
   * @param {string} moduleId
   * @param {boolean} enable
   * @param {func} cb
   */
  setEnabled(moduleId, enable = true, cb = null) {
    if (!moduleId) {
      return null;
    }
    const uiKey = BackendModuleManager.UI_KEY_MODULES;
    return (dispatch, getState) => {
      dispatch(this.requestEntity(moduleId, uiKey));
      this.getService().setEnabled(moduleId, enable)
      .then(json => {
        const installedModules = DataManager.getData(getState(), BackendModuleManager.UI_KEY_MODULES);
        if (installedModules.has(json.id)) {
          installedModules.get(json.id).disabled = json.disabled;
        }
        dispatch(this.dataManager.receiveData(uiKey, installedModules));
        dispatch(backendConfigurationInit());
        if (cb) {
          cb(json, null);
        }
      })
      .catch(error => {
        dispatch(this.receiveError({ id: moduleId, disabled: !enable }, uiKey, error, cb));
      });
    };
  }

  /**
   * Get result codes for module
   * @param {string} moduleId
   */
  fetchResultCodes(moduleId, searchParameters, cb = null) {
    if (!moduleId) {
      return null;
    }
    const uiKey = BackendModuleManager.UI_KEY_RESULT_CODES;
    return (dispatch) => {
      dispatch(this.requestEntity(moduleId, uiKey));
      this.getService().getResultCodes(moduleId, searchParameters)
      .then(json => {
        let resultCodes = new Immutable.Map();
        json.forEach(item => {
          resultCodes = resultCodes.set(item.id, item);
        });
        dispatch(this.dataManager.receiveData(uiKey, resultCodes));
      })
      .catch(error => {
        dispatch(this.receiveError({ id: moduleId }, uiKey, error, cb));
      });
    };
  }
}

BackendModuleManager.UI_KEY_MODULES = 'installed-modules';
BackendModuleManager.CODE_MODULE_ID = 'core';
BackendModuleManager.APP_MODULE_ID = 'app';
BackendModuleManager.UI_KEY_RESULT_CODES = 'result-codes';
