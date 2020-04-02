import { Services, Domain } from 'czechidm-core';

export default class SchemaAttributeService extends Services.AbstractService {

  // dto
  supportsPatch() {
    return false;
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    let objectClass = entity.objectClass ? entity.objectClass.objectClassName : null;
    if (!objectClass) {
      objectClass = (entity._embedded && entity._embedded.objectClass ? entity._embedded.objectClass.objectClassName : '');
    }
    if (objectClass) {
      return `${entity.name} (${objectClass})`;
    }
    return `${entity.name}`;
  }

  getApiPath() {
    return '/schema-attributes';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }
}
