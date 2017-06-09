import _ from 'lodash';
import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import { ConfigurationService } from '../../services';
import DataManager from './DataManager';
import { Actions, Properties } from '../config/constants';

export default class ConfigurationManager extends EntityManager {

  constructor() {
    super();
    this.service = new ConfigurationService();
    this.dataManager = new DataManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Configuration';
  }

  getCollectionType() {
    return 'configurations';
  }

  fetchPublicConfigurations(cb = null) {
    const uiKey = ConfigurationManager.PUBLIC_CONFIGURATIONS;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getPublicConfigurations()
        .then(json => {
          let publicConfigurations = new Immutable.Map();
          json.forEach(item => {
            publicConfigurations = publicConfigurations.set(item.name, item);
          });
          // receive public configurations
          dispatch({
            type: Actions.CONFIGURATION_RECEIVED,
            data: publicConfigurations
          });
          dispatch(this.dataManager.stopRequest(uiKey));
          if (cb) {
            cb(publicConfigurations);
          }
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.receiveError(null, uiKey, error, cb));
        });
    };
  }

  fetchAllConfigurationsFromFile() {
    const uiKey = ConfigurationManager.FILE_CONFIGURATIONS;
    //
    return (dispatch, getState) => {
      const fileConfigurations = DataManager.getData(getState(), uiKey);
      if (fileConfigurations) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getAllConfigurationsFromFile()
          .then(json => {
            dispatch(this.dataManager.receiveData(uiKey, json));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.receiveError(null, uiKey, error));
          });
      }
    };
  }

  fetchAllConfigurationsFromEnvironment() {
    const uiKey = ConfigurationManager.ENVIRONMENT_CONFIGURATIONS;
    //
    return (dispatch, getState) => {
      const environmentConfigurations = DataManager.getData(getState(), uiKey);
      if (environmentConfigurations) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getAllConfigurationsFromEnvironment()
          .then(json => {
            dispatch(this.dataManager.receiveData(uiKey, json));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.receiveError(null, uiKey, error));
          });
      }
    };
  }

  /**
   * Returns true, if configurationName should be guarded (contains guarded token, password etc.)
   *
   * @param  {string} configurationName
   * @return {bool}
   */
  shouldBeGuarded(configurationName) {
    return _.intersection(_.split(configurationName, '.'), ConfigurationManager.GUARDED_PROPERTY_NAMES).length > 0;
  }

  /**
   * Returns true, if configurationName should be secured (contains idm.sec. prefix)
   *
   * @param  {string} configurationName
   * @return {bool}
   */
  shouldBeSecured(configurationName) {
    if (!configurationName) {
      return false;
    }
    return configurationName.lastIndexOf('idm.sec.', 0) === 0;
  }

  /**
   * Returns setting value
   */
  static getPublicValue(state, key) {
    const publicConfigurations = state.config.get(Properties.PROPERTIES);
    if (!publicConfigurations) {
      return null;
    }
    if (!publicConfigurations.has(key)) {
      return null;
    }
    return publicConfigurations.get(key).value;
  }

  /**
   * Returns setting value as boolean. Return false, when setting is null.
   *
   * @param  {redux state} state
   * @param  {string} key
   * @return {boolean}
   */
  static getPublicValueAsBoolean(state, key) {
    return ConfigurationManager.getPublicValue(state, key) === 'true';
  }

  /**
   * Returns true, when module is enabled, false when disabled, null when configuration is not found.
   */
  static isModuleEnabled(state, moduleId) {
    const isModuleEnabled = ConfigurationManager.getPublicValue(state, `idm.pub.${moduleId}.enabled`);
    if (isModuleEnabled === null) {
      return null;
    }
    return isModuleEnabled === 'true';
  }

  /**
   * Returns environment stage [development, production, test]
   *
   * @param  {redux state} state
   * @return {string} stage [development, production, test]
   */
  static getEnvironmentStage(state) {
    const environment = ConfigurationManager.getPublicValue(state, 'idm.pub.app.stage');
    if (environment) {
      return environment.toLowerCase();
    }
    return null;
  }
}

ConfigurationManager.PUBLIC_CONFIGURATIONS = 'public-configurations'; // ui key only
ConfigurationManager.ENVIRONMENT_CONFIGURATIONS = 'environment-configurations'; // ui key to data redux
ConfigurationManager.FILE_CONFIGURATIONS = 'file-configurations'; // ui key to data redux
ConfigurationManager.GUARDED_PROPERTY_NAMES = ['password', 'token']; // automatically guarded property names
