import EntityManager from './EntityManager';
import { NotificationRecipientService } from '../../services';

/**
 * Notification recipient manager.
 *
 * @author Peter Šourek
 */
export default class NotificationRecipientManager extends EntityManager {

  constructor() {
    super();
    this.service = new NotificationRecipientService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'NotificationRecipient';
  }

  getCollectionType() {
    return 'recipients';
  }
}
