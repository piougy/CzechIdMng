import { Managers } from 'czechidm-core';
import { ProvisioningOperationService } from '../services';

const service = new ProvisioningOperationService();

export default class ProvisioningOperationManager extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'ProvisioningOperation'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'provisioningOperations';
  }
}
