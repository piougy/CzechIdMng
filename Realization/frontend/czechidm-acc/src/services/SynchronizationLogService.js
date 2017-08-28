import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';

export default class SynchronizationLogService extends Services.AbstractService {

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
    return entity.started;
  }

  getApiPath() {
    return '/system-synchronization-logs';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('started', 'DESC');
  }
}
