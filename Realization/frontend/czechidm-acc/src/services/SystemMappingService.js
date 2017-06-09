import { Services, Domain } from 'czechidm-core';
import SystemEntityTypeEnum from '../domain/SystemEntityTypeEnum';
import SystemOperationTypeEnum from '../domain/SystemOperationTypeEnum';

export default class SystemMappingService extends Services.AbstractService {

  constructor() {
    super();
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.name} (${SystemEntityTypeEnum.getNiceLabel(entity.entityType)} - ${SystemOperationTypeEnum.getNiceLabel(entity.operationType)})`;
  }

  getApiPath() {
    return '/system-mappings';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('entityType');
  }
}
