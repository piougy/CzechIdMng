import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';
import SystemEntityTypeEnum from '../domain/SystemEntityTypeEnum';

export default class SystemEntityService extends Services.AbstractService {

  constructor() {
    super();
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.system.name}:${SystemEntityTypeEnum.getNiceLabel(entity.entityType)}:${entity.uid}`;
  }

  getApiPath() {
    return '/systemEntities';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('uid');
  }
}
