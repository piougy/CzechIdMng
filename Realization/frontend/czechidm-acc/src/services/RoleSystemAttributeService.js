import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';

export default class RoleSystemAttributeService extends Services.AbstractRequestService {

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
    return `${entity.name}`;
  }

  getSubApiPath() {
    return '/role-system-attributes';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }
}
