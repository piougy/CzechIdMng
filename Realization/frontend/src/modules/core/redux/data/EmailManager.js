import EntityManager from './EntityManager';
import { EmailService } from '../../services';

export default class EmailManager extends EntityManager {

  constructor() {
    super();
    this.service = new EmailService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Email';
  }

  getCollectionType() {
    return 'emails';
  }
}
