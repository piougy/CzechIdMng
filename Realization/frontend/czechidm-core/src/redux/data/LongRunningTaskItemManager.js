import EntityManager from './EntityManager';
import LongRunningTaskItemService from '../../services/LongRunningTaskItemService';

/**
 * Long running task item administration
 *
 * @author Marek Klement
 */
export default class LongRunningTaskItemManager extends EntityManager {

  constructor() {
    super();
    this.service = new LongRunningTaskItemService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'ProcessedItem';
  }

  getCollectionType() {
    return 'processedTaskItems';
  }

  addToQueue(entityId, scheduledTask) {
    return this.getService().addToQueue(entityId, scheduledTask);
  }
}
