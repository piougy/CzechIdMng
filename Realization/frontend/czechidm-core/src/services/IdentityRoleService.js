import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RoleService from './RoleService';
import IdentityService from './IdentityService';

/**
 * Identity roles
 *
 * @author Radek Tomiška
 *
 */
export default class IdentityRoleService extends AbstractService {


  constructor() {
    super();
    this.roleService = new RoleService();
    this.identityService = new IdentityService();
  }

  getApiPath() {
    return '/identity-roles';
  }

  getGroupPermission() {
    return 'IDENTITYROLE';
  }

  /**
   * Extended nice label
   *
   * @param  {entity} entity
   * @param  {boolean} showIdentity identity will be rendered.
   * @return {string}
   */
  getNiceLabel(entity, showIdentity = true) {
    if (!entity) {
      return '';
    }
    if (!entity._embedded) {
      return entity.id;
    }
    let niceLabel = null;
    if (showIdentity && entity._embedded.identityContract && entity._embedded.identityContract._embedded && entity._embedded.identityContract._embedded.identity) {
      niceLabel = this.identityService.getNiceLabel(entity._embedded.identityContract._embedded.identity);
    }
    if (entity._embedded.role) {
      if (niceLabel !== null) {
        niceLabel += ' - ';
      } else {
        niceLabel = '';
      }
      niceLabel += this.roleService.getNiceLabel(entity._embedded.role);
    }
    return niceLabel;
  }

  /**
  * Returns default searchParameters for current entity type
  *
  * @return {object} searchParameters
  */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('role.name');
  }
}
