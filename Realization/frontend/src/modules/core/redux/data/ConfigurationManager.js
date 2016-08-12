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

  fetchFileConfigurations() {
    const uiKey = ConfigurationManager.FILE_CONFIGURATIONS;
    //
    return (dispatch, getState) => {
      const fileConfigurations = DataManager.getData(getState(), uiKey);
      if (fileConfigurations) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getFileConfigurations()
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
}

ConfigurationManager.PUBLIC_CONFIGURATIONS = 'public-configurations';
ConfigurationManager.FILE_CONFIGURATIONS = 'file-configurations';
