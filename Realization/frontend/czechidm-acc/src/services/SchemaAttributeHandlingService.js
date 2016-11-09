import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';

export default class SchemaAttributeHandlingService extends Services.AbstractService {

  constructor() {
    super();
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.idmPropertyName;
  }

  getApiPath() {
    return '/schema-attributes-handling';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('idmPropertyName');
  }
}
