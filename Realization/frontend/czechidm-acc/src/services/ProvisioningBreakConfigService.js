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
    if (entity._embedded && entity._embedded.system) {
      return `${entity._embedded.system.name}:${ProvisioningOperationTypeEnum.getNiceLabel(entity.operationType)}`;
    }
    return Services.LocalizationService.i18n('acc:entity.ProvisioningBreakConfig.globalConfiguration') + ':' + ProvisioningOperationTypeEnum.getNiceLabel(entity.operationType);
  }

  getApiPath() {
    return '/provisioning-break-configs';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort();
  }
}
