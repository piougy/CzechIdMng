import EntityManager from './EntityManager';
import { TokenService } from '../../services';

/**
 * Tokens.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
export default class TokenManager extends EntityManager {

  constructor() {
    super();
    this.service = new TokenService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Token';
  }

  getCollectionType() {
    return 'tokens';
  }
}
