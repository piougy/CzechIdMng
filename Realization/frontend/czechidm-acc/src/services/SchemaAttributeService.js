import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';

export default class SchemaAttributeService extends Services.AbstractService {

  constructor() {
    super();
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  getApiPath() {
    return '/schema-attributes';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }
}
