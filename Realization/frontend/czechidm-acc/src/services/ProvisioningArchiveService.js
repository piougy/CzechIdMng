import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';
import ProvisioningOperationService from './ProvisioningOperationService';

/**
 * Archived provisioning operations.
 *
 * @author Radek Tomiška
 */
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

  getGroupPermission() {
    return 'PROVISIONINGARCHIVE';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('modified', 'desc');
  }
}
