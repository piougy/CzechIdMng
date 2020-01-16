import { Services, Domain } from 'czechidm-core';

export default class SyncActionLogService extends Services.AbstractService {

  // dto
  supportsPatch() {
    return false;
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.syncAction;
  }

  getApiPath() {
    return '/system-synchronization-action-logs';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('syncAction', false);
  }
}
