import EntityManager from './EntityManager';
import { EntityStateService } from '../../services';

/**
 * Entity states
 *
 * @author Radek Tomiška
 */
export default class EntityStateManager extends EntityManager {

  constructor() {
    super();
    this.service = new EntityStateService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'EntityState';
  }

  getCollectionType() {
    return 'entityStates';
  }
}
