import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';
import ProvisioningOperationTypeEnum from '../domain/ProvisioningOperationTypeEnum';

export default class ProvisioningBreakConfigService extends Services.AbstractService {

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
    return `${entity._embedded.system.name}:${ProvisioningOperationTypeEnum.getNiceLabel(entity.entityType)}`;
  }

  getApiPath() {
    return '/provisioning-break-configs';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort();
  }
}
