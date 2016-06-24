'use strict'

import EntityManager from '../../modules/core/redux/data/EntityManager';
import { AuditLogService } from '../../services';

const service = new AuditLogService();

export default class AuditLogManager extends EntityManager {

  constructor () {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'AuditLog'; // TODO: constant or enumeration
  }
}
