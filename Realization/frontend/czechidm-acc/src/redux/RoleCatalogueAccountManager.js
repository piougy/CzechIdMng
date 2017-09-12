import { Managers } from 'czechidm-core';
import { RoleCatalogueAccountService } from '../services';

const service = new RoleCatalogueAccountService();

/**
 * Role catalogue accounts
 *
 * @author Kučera
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
