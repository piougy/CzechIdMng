import EntityManager from './EntityManager';
import { PasswordHistoryService } from '../../services';

/**
 * Password history
 *
 * @author Ondrej Kopr
 * @since 9.5.0
 */
export default class PasswordHistoryManager extends EntityManager {

  constructor() {
    super();
    this.service = new PasswordHistoryService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'PasswordHistory';
  }

  getCollectionType() {
    return 'passwordHistories';
  }

}
