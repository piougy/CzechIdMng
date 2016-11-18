import { Managers } from 'czechidm-core';
import { IdentityAccountService } from '../services';

const service = new IdentityAccountService();

export default class IdentityAccountManager extends Managers.EntityManager {

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
    return 'IdentityAccount'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'identityAccounts';
  }
}
