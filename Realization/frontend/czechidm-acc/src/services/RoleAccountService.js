import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';
import AccountTypeEnum from '../domain/AccountTypeEnum';

export default class RoleAccountService extends Services.AbstractService {

  constructor() {
    super();
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${AccountTypeEnum.getNiceLabel(entity._embedded.account.accountType)}:${entity._embedded.account._embedded.system.name}:${entity._embedded.account._embedded.systemEntity ? entity._embedded.account._embedded.systemEntity.uid : ''}`;
  }

  getApiPath() {
    return '/role-accounts';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('account.uid');
  }
}
