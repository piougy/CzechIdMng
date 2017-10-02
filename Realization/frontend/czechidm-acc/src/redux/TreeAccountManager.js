import { Managers } from 'czechidm-core';
import { TreeAccountService } from '../services';

const service = new TreeAccountService();

/**
 * Tree accounts
 *
 * @author Kučera
 */
export default class TreeAccountManager extends Managers.EntityManager {
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
    return 'TreeAccount';
  }

  getCollectionType() {
    return 'treeAccounts';
  }
}
