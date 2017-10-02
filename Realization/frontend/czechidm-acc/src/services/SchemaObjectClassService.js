import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';

export default class SchemaObjectClassService extends Services.AbstractService {

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
    return entity.objectClassName;
  }

  getApiPath() {
    return '/schema-object-classes';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('objectClassName');
  }
}
