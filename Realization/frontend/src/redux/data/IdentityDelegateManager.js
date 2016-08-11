import EntityManager from '../../modules/core/redux/data/EntityManager';
import { IdentityDelegateService } from '../../services';

/**
 * Manager for identity's delegates
 */
export default class IdentityDelegateManager extends EntityManager {

  constructor(username) {
    super();
    this.identityDelegateService = new IdentityDelegateService(username);
  }

   setUsername(username) {
     this.getService().setUsername(username);
   }

  getService() {
    return this.identityDelegateService;
  }

  getEntityType() {
    return 'IdentityDelegate'; // TODO: constant or enumeration
  }

  getDelegates(uiKey, cb = null) {
    return this.fetchEntities({}, uiKey, cb);
  }
}
