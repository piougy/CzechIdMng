import EntityManager from './EntityManager';
import { NotificationConfigurationService } from '../../services';

export default class NotificationConfigurationManager extends EntityManager {

  constructor() {
    super();
    this.service = new NotificationConfigurationService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'NotificationConfiguration';
  }

  getCollectionType() {
    return 'notificationConfigurations';
  }
}
