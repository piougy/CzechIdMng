import EntityManager from './EntityManager';
import { ScriptAuthorityService } from '../../services';
import DataManager from './DataManager';
/**
 * Script authorities
 *
 */
export default class ScriptAuthorityManager extends EntityManager {

  constructor() {
    super();
    this.service = new ScriptAuthorityService();
    this.dataManager = new DataManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'ScriptAuthority';
  }

  getCollectionType() {
    return 'scriptAuthorities';
  }

  fetchAvailableServices(uiKey) {
    return (dispatch, getState) => {
      const availableAuthorities = DataManager.getData(getState(), uiKey);
      if (availableAuthorities) {
        // we dont need to load them again - identity needs to be logged in / out
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getAvailableServices()
          .then(json => {
            dispatch(this.dataManager.receiveData(uiKey, json));
          })
          .catch(error => {
            dispatch(this.receiveError(null, uiKey, error));
          });
      }
    };
  }
}
