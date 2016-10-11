

import EntityManager from './EntityManager';
import { IdentityContractService, IdentityService } from '../../services';

const service = new IdentityContractService();
const identityService = new IdentityService();

export default class IdentityContractManager extends EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'IdentityContract'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'identityContracts';
  }

  // TODO: use force filters and search instread? Security on identityContracts endpoint?
  fetchContracts(username, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey);
    return (dispatch) => {
      dispatch(this.requestEntities(null, uiKey));
      identityService.getContracts(username)
      .then(json => {
        dispatch(this.receiveEntities(null, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError({}, uiKey, error, cb));
      });
    };
  }
}
