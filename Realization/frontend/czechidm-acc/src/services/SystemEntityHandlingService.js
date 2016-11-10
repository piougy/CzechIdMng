import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';
import SystemEntityTypeEnum from '../domain/SystemEntityTypeEnum';
import SystemOperationTypeEnum from '../domain/SystemOperationTypeEnum';

export default class SchemaAttributeHandlingService extends Services.AbstractService {

  constructor() {
    super();
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${SystemEntityTypeEnum.getNiceLabel(entity.entityType)} (${SystemOperationTypeEnum.getNiceLabel(entity.operationType)})`;
  }

  getApiPath() {
    return '/system-entities-handling';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('entityType');
  }
}
