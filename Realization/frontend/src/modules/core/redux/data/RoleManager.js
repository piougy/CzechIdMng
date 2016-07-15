'use strict'

import EntityManager from './EntityManager';
import { RoleService } from '../../services';
import DataManager from './DataManager';

export default class RoleManager extends EntityManager {

  constructor () {
    super();
    this.service = new RoleService();
    this.dataManager = new DataManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Role';
  }

  getCollectionType() {
    return 'roles';
  }

  /**
   * Load available authorities from BE if needed (available authorites can be changed just with BE restart)
   * 
   * @param  {string} uiKey
   * @return {array[object]}
   */
  fetchAvailableAuthorities(uiKey) {
    return (dispatch, getState) => {
      const availableAuthorities = DataManager.getData(getState(), uiKey);
      if (availableAuthorities) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getAvailableAuthorities()
          .then(json => {
            dispatch(this.dataManager.receiveData(uiKey, json));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.receiveError(null, uiKey, error));
          });
      }

    }
  }
}
