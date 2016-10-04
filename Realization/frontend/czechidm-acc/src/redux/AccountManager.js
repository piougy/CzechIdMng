import { Managers } from 'czechidm-core';
import { AccountService } from '../services';

const service = new AccountService();

export default class AccountManager extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'Account'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'accounts';
  }
}
