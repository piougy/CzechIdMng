import EntityManager from '../../modules/core/redux/data/EntityManager';
import { IdentityAccountService } from '../../services';

/**
 * Manager for setting fetching
 */
export default class IdentityAccountManager extends EntityManager {

  constructor(username) {
    super();
    this.identityAccountService = new IdentityAccountService(username);
   }

   setUsername(username) {
     this.getService().setUsername(username);
   }

  getService() {
    return this.identityAccountService;
  }

  getEntityType() {
    return 'IdentityAccount'; // TODO: constant or enumeration
  }

  /**
   * TODO: Move to IdentityManager
   *	Load accounts for identtity - but username is setted in constructor and this method works on hidden logic in service
   *
   * @deprecated
   * @param  {[type]} uiKey [description]
   * @param  {[type]} cb    =             null [description]
   * @return {[type]}       [description]
   */
  getAccounts(uiKey, cb = null) {
    return this.fetchEntities({}, uiKey, cb)
  }
}
