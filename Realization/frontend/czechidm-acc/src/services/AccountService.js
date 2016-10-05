import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';
import AccountTypeEnum from '../domain/AccountTypeEnum';

export default class AccountService extends Services.AbstractService {

  constructor() {
    super();
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${AccountTypeEnum.getNiceLabel(entity.accountType)}:${entity._embedded.system.name}:${entity._embedded.systemEntity ? entity._embedded.systemEntity.uid : ''}`;
  }

  getApiPath() {
    return '/accounts';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('systemEntity.uid');
  }
}
