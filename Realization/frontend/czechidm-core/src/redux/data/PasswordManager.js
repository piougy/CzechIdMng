import EntityManager from './EntityManager';
import { PasswordService } from '../../services';

export default class PasswordManager extends EntityManager {

  constructor() {
    super();
    this.service = new PasswordService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Password';
  }

  getCollectionType() {
    return 'passwords';
  }

  /**
   * Load password by identity id from server
   *
   * @param  {string|number} identityIdentifier - Identity identifier
   * @param  {string} uiKey = null - ui key for loading indicator etc
   * @return {object} - action
   */
  fetchPasswordByIdentity(identityIdentifier, uiKey = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getPassword(identityIdentifier)
        .then(json => {
          dispatch(this.dataManager.receiveData(uiKey, json));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }

}
