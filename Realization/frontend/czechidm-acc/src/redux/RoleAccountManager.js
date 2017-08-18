import { Managers } from 'czechidm-core';
import { RoleAccountService } from '../services';

const service = new RoleAccountService();

/**
 * Role accounts
 *
 * @author Roman Kučera
 */
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

  getEntityType() {
    return 'RoleAccount';
  }

  getCollectionType() {
    return 'roleAccounts';
  }
}
