import EntityManager from './EntityManager';
import { AuditService } from '../../services';

export default class AuditManager extends EntityManager {

  constructor() {
    super();
    this.service = new AuditService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Audit';
  }

  getCollectionType() {
    return 'audits';
  }

  /**
   * Return seearch paramaters for endpoind with information about audited entities.
   */
  getAuditedEntitiesNames() {
    return this.getService().getAuditedEntitiesSearchParameters();
  }
}
