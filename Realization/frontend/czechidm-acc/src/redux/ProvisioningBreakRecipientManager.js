import { Managers } from 'czechidm-core';
import { ProvisioningBreakRecipientService } from '../services';

const service = new ProvisioningBreakRecipientService();

export default class ProvisioningBreakRecipientManager
 extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'ProvisioningBreakRecipient';
  }

  getCollectionType() {
    return 'provisioningBreakRecipients';
  }
}
