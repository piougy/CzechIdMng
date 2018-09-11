import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';

export default class RoleSystemService extends Services.AbstractRequestService {

  constructor() {
    super();
  }

  supportsPatch() {
    return false;
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity._embedded.role.name} - ${entity._embedded.system.name} (${entity._embedded.systemMapping.entityType})`;
  }

  getSubApiPath() {
    return '/role-systems';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('system.name');
  }
}
