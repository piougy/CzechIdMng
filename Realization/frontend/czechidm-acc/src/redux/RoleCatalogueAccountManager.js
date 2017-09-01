import { Managers } from 'czechidm-core';
import { RoleAccountService } from '../services';

const service = new RoleAccountService();

/**
 * Role catalogue accounts
 *
 * @author Roman Kučera
 */
export default class RoleCatalogueAccountManager extends Managers.EntityManager {

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
    return 'RoleCatalogueAccount';
  }

  getCollectionType() {
    return 'roleCatalogueAccounts';
  }
}
