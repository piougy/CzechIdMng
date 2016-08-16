import _ from 'lodash';
//
import EntityManager from './EntityManager';
import { ConfigurationService } from '../../services';
import DataManager from './DataManager';

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

  fetchPublicConfigurations() {
    const uiKey = ConfigurationManager.PUBLIC_CONFIGURATIONS;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getPublicConfigurations()
        .then(json => {
          dispatch(this.dataManager.receiveData(uiKey, json));
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.receiveError(null, uiKey, error));
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
}

ConfigurationManager.PUBLIC_CONFIGURATIONS = 'public-configurations';
ConfigurationManager.FILE_CONFIGURATIONS = 'file-configurations';
ConfigurationManager.ENVIRONMENT_CONFIGURATIONS = 'environment-configurations';
ConfigurationManager.GUARDED_PROPERTY_NAMES = ['password', 'token'];
