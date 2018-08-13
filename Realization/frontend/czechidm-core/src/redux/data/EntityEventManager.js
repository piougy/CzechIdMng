import EntityManager from './EntityManager';
import { EntityEventService } from '../../services';

/**
 * Entity events and states
 *
 * @author Radek Tomiška
 */
export default class EntityEventManager extends EntityManager {

  constructor() {
    super();
    this.service = new EntityEventService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'EntityEvent';
  }

  getCollectionType() {
    return 'entityEvents';
  }

  /**
   * Delete all entity events
   *
   * @return {Promise}
   */
  deleteAll(uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey);
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().deleteAll()
        .then(json => {
          dispatch(this.dataManager.receiveData(uiKey, json, cb));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error, cb));
        });
    };
  }
}
