import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';
import AccountTypeEnum from '../domain/AccountTypeEnum';

export default class IdentityAccountService extends Services.AbstractService {

  constructor() {
    super();
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${AccountTypeEnum.getNiceLabel(entity.account.type)}:${entity.account._embedded.system.name}:${entity.account._embedded.systemEntity ? entity.account._embedded.systemEntity.uid : ''}`;
  }

  getApiPath() {
    return '/identityAccounts';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('account.systemEntity.uid');
  }
}
