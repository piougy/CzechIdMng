import { Managers } from 'czechidm-core';
import { RoleAccountService } from '../services';

const service = new RoleAccountService();

export default class RoleAccountManager extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getNiceLabelWithSystem(systemName, username) {
    return systemName + ' (' + username + ')';
  }

  supportsPatch() {
    return false;
  }

  getEntityType() {
    return 'RoleAccount';
  }

  getCollectionType() {
    return 'roleAccounts';
  }
}
