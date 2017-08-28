import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';

export default class RoleSystemService extends Services.AbstractService {

  constructor() {
    super();
  }

  getNiceLabel(entity) {
    // TODO uncomment comment with systemMapping.entityType and remove current row after refactor roleSystem to DTO!
    if (!entity) {
      return '';
    }
    if (!entity._embedded) {
      // return `${entity.role.name} - ${entity.system.name} (${entity.systemMapping.entityType})`;
      return `${entity.role.name} - ${entity.system.name}`;
    }
    // return `${entity._embedded.role.name} - ${entity._embedded.system.name} (${entity._embedded.systemMapping.entityType})`;
    return `${entity._embedded.role.name} - ${entity._embedded.system.name}`;
  }

  getApiPath() {
    return '/role-systems';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('system.name');
  }
}
