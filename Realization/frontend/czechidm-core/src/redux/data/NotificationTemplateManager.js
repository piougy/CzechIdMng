import EntityManager from './EntityManager';
import { NotificationTemplateService } from '../../services';

export default class TreeTypeManager extends EntityManager {

  constructor() {
    super();
    this.service = new NotificationTemplateService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'NotificationTemplate';
  }

  getCollectionType() {
    return 'notificationTemplates';
  }
}
