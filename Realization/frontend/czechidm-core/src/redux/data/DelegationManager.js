import EntityManager from './EntityManager';
import { DelegationService } from '../../services';

/**
 * Delegation manager.
 *
 * @author Vít Švanda
 * @since 10.4.0
 */
export default class DelegationManager extends EntityManager {

  constructor() {
    super();
    this.service = new DelegationService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Delegation';
  }

  getCollectionType() {
    return 'delegations';
  }

}
