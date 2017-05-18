import EntityManager from './EntityManager';
import { SmsService } from '../../services';

/**
 * Sms logs
 *
 * @author Peter Sourek
 */
export default class SmsManager extends EntityManager {

  constructor() {
    super();
    this.service = new SmsService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'SmsLog';
  }

  getCollectionType() {
    return 'smsLogs';
  }
}
