'use strict'

import EntityManager from './EntityManager';
import { RoleService } from '../../services';

export default class RoleManager extends EntityManager {

  constructor () {
    super();
    this.service = new RoleService();
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

  fetchAvailableAuthorities(cb) {
    const uiKey = 'available-authorities'
    return (dispatch, getState) => {
      dispatch(this.requestEntity(null, 'uiKey'));
      this.getService().getAvailableAuthorities()
      .then(json => {
        // TODO: fetch / request / receive data
        cb(json);
      })
      .catch(error => {
        dispatch(this.receiveError(null, uiKey, error));
      });
    }
  }
}
