import { Services, Domain } from 'czechidm-core';

export default class SchemaObjectClassService extends Services.AbstractService {

  // dto
  supportsPatch() {
    return false;
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.objectClassName;
  }

  getApiPath() {
    return '/schema-object-classes';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('objectClassName');
  }
}
