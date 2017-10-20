import { Managers } from 'czechidm-core';
import { ContractAccountService } from '../services';

const service = new ContractAccountService();

/**
 * Contract accounts
 *
 * @author Vít Švanda
 */
export default class ContractAccountManager extends Managers.EntityManager {

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
    return 'ContractAccount';
  }

  getCollectionType() {
    return 'contractAccounts';
  }
}
