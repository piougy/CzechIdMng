import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';
import AccountTypeEnum from '../domain/AccountTypeEnum';


/**
 * Tree accounts
 *
 * @author Kučera
 */
export default class TreeAccountService extends Services.AbstractService {

  constructor() {
    super();
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${AccountTypeEnum.getNiceLabel(entity._embedded.account.accountType)}:${entity._embedded.account._embedded.system.name}:${entity._embedded.account.uid}`;
  }

  getApiPath() {
    return '/tree-accounts';
  }

  supportsPatch() {
    return false;
  }

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'TREEACCOUNT';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('account.uid');
  }
}
