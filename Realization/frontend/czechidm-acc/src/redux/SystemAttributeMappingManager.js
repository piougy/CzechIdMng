import { Managers } from 'czechidm-core';
import { SystemAttributeMappingService } from '../services';

const service = new SystemAttributeMappingService();

export default class SystemAttributeMappingManager
 extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'SystemAttributeMapping';
  }

  getCollectionType() {
    return 'systemAttributeMappings';
  }
}
