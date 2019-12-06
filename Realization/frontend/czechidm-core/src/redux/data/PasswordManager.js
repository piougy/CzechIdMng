import EntityManager from './EntityManager';
import { PasswordService } from '../../services';

/**
 * @author Ondřej Kopr
 *
 * @since 9.6.0
 */
export default class PasswordManager extends EntityManager {

  constructor() {
    super();
    this.service = new PasswordService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Password';
  }

  getCollectionType() {
    return 'passwords';
  }
}
