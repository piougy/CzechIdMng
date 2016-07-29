import EntityManager from './EntityManager';
import { NotificationService } from '../../services';

export default class NotificationManager extends EntityManager {

  constructor() {
    super();
    this.service = new NotificationService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Notification';
  }

  getCollectionType() {
    return 'notifications';
  }
}
