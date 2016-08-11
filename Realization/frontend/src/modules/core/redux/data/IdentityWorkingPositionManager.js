

import EntityManager from './EntityManager';
import { IdentityWorkingPositionService, IdentityService } from '../../services';

const service = new IdentityWorkingPositionService();
const identityService = new IdentityService();

export default class IdentityWorkingPositionManager extends EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'IdentityWorkingPosition'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'workingPositions';
  }

  fetchWorkingPositions(username, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey);
    return (dispatch) => {
      dispatch(this.requestEntities(null, uiKey));
      identityService.getWorkingPositions(username)
      .then(json => {
        dispatch(this.receiveEntities(null, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError({}, uiKey, error, cb));
      });
    };
  }
}
