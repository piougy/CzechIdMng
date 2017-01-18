import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';
import ProvisioningOperationService from './ProvisioningOperationService';

export default class ProvisioningArchiveService extends Services.AbstractService {

  constructor() {
    super();
    this.operationService = new ProvisioningOperationService();
  }

  getNiceLabel(entity) {
    return this.operationService.getNiceLabel(entity);
  }

  getApiPath() {
    return '/provisioning-archives';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }
}
