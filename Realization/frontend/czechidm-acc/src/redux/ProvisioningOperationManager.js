import { Managers } from 'czechidm-core';
import { ProvisioningOperationService } from '../services';

const service = new ProvisioningOperationService();

export default class ProvisioningOperationManager extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'ProvisioningOperation'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'provisioningOperations';
  }

  /**
   * Delete all provisioning operation from queue for the given system identifier.
   *
   * @param  {string} system
   * @return {Promise}
   */
  deleteAll(system = null, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey);
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().deleteAll(system)
        .then(json => {
          dispatch(this.dataManager.receiveData(uiKey, json, cb));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error, cb));
        });
    };
  }
}
