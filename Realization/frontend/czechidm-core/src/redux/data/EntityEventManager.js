import EntityManager from './EntityManager';
import { EntityEventService } from '../../services';

/**
 * Entity events and states
 *
 * @author Radek Tomiška
 */
export default class EntityEventManager extends EntityManager {

  constructor() {
    super();
    this.service = new EntityEventService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'EntityEvent';
  }

  getCollectionType() {
    return 'entityEvents';
  }
}
