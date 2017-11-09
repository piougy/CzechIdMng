import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';

export default class ProvisioningBreakRecipientService extends Services.AbstractService {

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
    if (entity._embedded && entity._embedded.identity) {
      return `${entity._embedded.identity.username}`;
    }
    return `${entity._embedded.role.name}`;
  }

  getApiPath() {
    return '/provisioning-break-recipients';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort();
  }
}
