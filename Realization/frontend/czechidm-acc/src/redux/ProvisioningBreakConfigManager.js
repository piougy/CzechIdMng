import { Managers } from 'czechidm-core';
import { ProvisioningBreakConfigService } from '../services';

const service = new ProvisioningBreakConfigService();

export default class ProvisioningBreakConfigManager
 extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'ProvisioningBreakConfig';
  }

  getCollectionType() {
    return 'provisioningBreakConfigs';
  }
}
