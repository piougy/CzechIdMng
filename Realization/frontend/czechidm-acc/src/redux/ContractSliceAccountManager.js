import { Managers } from 'czechidm-core';
import { ContractSliceAccountService } from '../services';

const service = new ContractSliceAccountService();

/**
 * ContractSlice slice accounts
 *
 * @author Vít Švanda
 */
export default class ContractSliceAccountManager extends Managers.EntityManager {

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
    return 'ContractSliceAccount';
  }

  getCollectionType() {
    return 'contractSliceAccounts';
  }
}
