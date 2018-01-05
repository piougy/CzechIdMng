import { Managers } from 'czechidm-core';
import { SystemMappingService } from '../services';

const service = new SystemMappingService();

export default class SystemMappingManager
 extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'SystemMapping';
  }

  getCollectionType() {
    return 'systemMappings';
  }

  /**
   * Validates system mappping
   *
   * @param systemMappingId {String}
   */
  validate(systemMappingId) {
    return service.validate(systemMappingId);
  }
}
