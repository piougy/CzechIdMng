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
  realize(entity, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey, entity.id);
    return (dispatch) => {
      dispatch(this.requestEntity(entity.id, uiKey));
      this.getService().realize(entity.id)
      .then(json => {
        dispatch(this.receiveEntity(entity.id, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(entity, uiKey, error, cb));
      });
    };
  }

  /**
  * Cancel virtual system request
  */
  cancel(entity, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey, entity.id);
    return (dispatch) => {
      dispatch(this.requestEntity(entity.id, uiKey));
      this.getService().cancel(entity.id)
      .then(json => {
        dispatch(this.receiveEntity(entity.id, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(entity, uiKey, error, cb));
      });
    };
  }
}
