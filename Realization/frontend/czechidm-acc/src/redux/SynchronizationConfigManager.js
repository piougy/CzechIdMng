import { Managers } from 'czechidm-core';
import { SynchronizationConfigService } from '../services';

const service = new SynchronizationConfigService();

/**
 * Configured synchronizations.
 *
 * @author Vít Švanda
 */
export default class SynchronizationConfigManager extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'SynchronizationConfig';
  }

  getCollectionType() {
    return 'synchronizationConfigs';
  }
}
