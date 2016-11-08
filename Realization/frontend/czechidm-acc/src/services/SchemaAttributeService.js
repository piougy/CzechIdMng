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
    let objectClass = entity.objectClass ? entity.objectClass.objectClassName : null;
    if (!objectClass) {
      objectClass = (entity._embedded && entity._embedded.objectClass ? entity._embedded.objectClass.objectClassName : '');
    }
    return `${entity.name} (${objectClass})`;
  }

  getApiPath() {
    return '/schema-attributes';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }
}
