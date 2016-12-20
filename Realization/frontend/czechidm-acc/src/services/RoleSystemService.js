import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';

export default class RoleSystemService extends Services.AbstractService {

  constructor() {
    super();
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    if (!entity._embedded) {
      return `${entity.role.name} - ${entity.system.name} (${entity.systemEntityHandling.entityType})`;
    }
    return `${entity._embedded.role.name} - ${entity._embedded.system.name} (${entity._embedded.systemEntityHandling.entityType})`;
  }

  getApiPath() {
    return '/role-systems';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('system.name');
  }
}
