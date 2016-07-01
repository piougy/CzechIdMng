'use strict';

import EntityManager from './EntityManager';
import { IdentityRoleService, IdentityService } from '../../services';

const service = new IdentityRoleService();
const identityService = new IdentityService();

export default class IdentityRoleManager extends EntityManager {

  constructor () {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'IdentityRole'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'identityRoles';
  }

  fetchRoles(username, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey);
    return (dispatch, getState) => {
      dispatch(this.requestEntities(null, uiKey));
      identityService.getRoles(username)
      .then(json => {
        dispatch(this.receiveEntities(null, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError({}, uiKey, error, cb));
      });
    }
  }
}
