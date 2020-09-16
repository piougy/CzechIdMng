import EntityManager from './EntityManager';
import { NotificationAttachmentService } from '../../services';

/**
 * Notification attachments.
 *
 * @author Radek Tomiška
 * @since 10.6.0
 */
export default class NotificationAttachmentManager extends EntityManager {

  constructor() {
    super();
    this.service = new NotificationAttachmentService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'NotificationAttachment';
  }

  getCollectionType() {
    return 'notificationAttachments';
  }
}
