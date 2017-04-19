import EntityManager from './EntityManager';
import { ScriptAuthorityService } from '../../services';

/**
 * Script authorities
 *
 */
export default class ScriptAuthorityManager extends EntityManager {

  constructor() {
    super();
    this.service = new ScriptAuthorityService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'ScriptAuthority';
  }

  getCollectionType() {
    return 'scriptAuthorities';
  }
}
