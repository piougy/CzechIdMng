import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';

export default class SyncItemLogService extends Services.AbstractService {

  constructor() {
    super();
  }

  // dto
  supportsPatch() {
    return false;
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.displayName;
  }

  getApiPath() {
    return '/system-synchronization-item-logs';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort();
  }
}
