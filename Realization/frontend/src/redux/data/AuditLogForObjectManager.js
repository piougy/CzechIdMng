'use strict'

import EntityManager from '../../modules/core/redux/data/EntityManager';
import { AuditLogForObjectService } from '../../services';

const service = new AuditLogForObjectService();

export default class AuditLogForObjectManager extends EntityManager {

  constructor () {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'AuditLogForObject'; // TODO: constant or enumeration
  }
}
