import { Managers } from 'czechidm-core';
import { UniformPasswordService } from '../services';

const service = new UniformPasswordService();

/**
 * Unifor password definition manager for frontend.
 *
 * @author Ondrej Kopr
 */
export default class UniformPasswordManager extends Managers.EntityManager {

  getService() {
    return service;
  }

  getEntityType() {
    return 'UniformPassword';
  }

  getCollectionType() {
    return 'uniformPasswords';
  }

  fetchPasswordChangeOptions(entityId, uiKey = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getPasswordChangeOptions(entityId)
        .then(json => {
          if (json && json._embedded && json._embedded.uniformPasswordOptions) {
            dispatch(this.dataManager.receiveData(uiKey, json._embedded.uniformPasswordOptions));
          } else {
            dispatch(this.dataManager.receiveData(uiKey, json));
          }
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }

  getPasswordChangeOptions(state, uiKey = null) {
    return Managers.DataManager.getData(state, uiKey);
  }
}
