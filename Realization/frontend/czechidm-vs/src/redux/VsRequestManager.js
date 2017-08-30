import { Managers } from 'czechidm-core';
import { VsRequestService } from '../services';

/**
 * Manager controlls request for virtual systems
 *
 * @author Vít Švanda
 */
export default class VsRequestManager extends Managers.EntityManager {

  constructor() {
    super();
    this.service = new VsRequestService();
  }

  getModule() {
    return 'vs';
  }

  getService() {
    return this.service;
  }

  /**
   * Controlled entity
   */
  getEntityType() {
    return 'VsRequest';
  }

  /**
   * Collection name in search / find response
   */
  getCollectionType() {
    return 'requests';
  }

  /**
  * Mark virtual system request as realized (changes will be propagated to VsAccount)
  */
  realize(entityId, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey, entityId);
    return (dispatch) => {
      dispatch(this.requestEntity(entityId, uiKey));
      this.getService().realize(entityId)
      .then(json => {
        dispatch(this.receiveEntity(entityId, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(entityId, uiKey, error, cb));
      });
    };
  }

  /**
  * Cancel virtual system request
  */
  cancel(entityId, reason, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey, entityId);
    return (dispatch) => {
      dispatch(this.requestEntity(entityId, uiKey));
      this.getService().cancel(entityId, reason)
      .then(json => {
        dispatch(this.receiveEntity(entityId, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(entityId, uiKey, error, cb));
      });
    };
  }
}
