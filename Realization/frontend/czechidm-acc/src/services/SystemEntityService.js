import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';

export default class SystemEntityService extends Services.AbstractService {

  constructor() {
    super();
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.system.name}:${entity.entityType} (${entity.uid})`;
  }

  getApiPath() {
    return '/systemEntities';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('uid');
  }
}
