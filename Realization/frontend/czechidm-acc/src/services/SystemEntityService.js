import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';
import SystemEntityTypeEnum from '../domain/SystemEntityTypeEnum';

export default class SystemEntityService extends Services.AbstractService {

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
    return `${entity._embedded.system.name}:${SystemEntityTypeEnum.getNiceLabel(entity.entityType)}:${entity.uid}`;
  }

  getApiPath() {
    return '/system-entities';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('uid');
  }
}
