import EntityManager from './EntityManager';
import { NotificationRecipientService } from '../../services';

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
