import FormableEntityManager from './FormableEntityManager';
import { RoleService } from '../../services';
import DataManager from './DataManager';

/**
 * Operations with RoleService
 *
 * @author Radek Tomiška
 */
export default class RoleManager extends FormableEntityManager {

  constructor() {
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
  fetchAvailableAuthorities() {
    const uiKey = RoleManager.UI_KEY_AVAILABLE_AUTHORITIES;
    //
    return (dispatch, getState) => {
      const availableAuthorities = DataManager.getData(getState(), uiKey);
      if (availableAuthorities) {
        // we dont need to load them again - identity needs to be logged in / out
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
    };
  }
}

RoleManager.UI_KEY_AVAILABLE_AUTHORITIES = 'available-authorities';
