import AbstractRequestService from './AbstractRequestService';
import SearchParameters from '../domain/SearchParameters';
import RoleService from './RoleService';

const roleService = new RoleService();

/**
 * Role guarantees - by role
 *
 * @author Radek Tomiška
 */
export default class RoleGuaranteeRoleService extends AbstractRequestService {

  /**
   * Using in the request
   */
  getSubApiPath() {
    return '/role-guarantee-roles';
  }

  getNiceLabel(entity) {
    if (!entity || !entity._embedded) {
      return '';
    }
    let label = `${roleService.getNiceLabel(entity._embedded.role)}`;
    if (entity.guaranteeRole) {
      label += ` - ${roleService.getNiceLabel(entity._embedded.guaranteeRole)}`;
    }
    //
    return label;
  }

  supportsPatch() {
    if (this.isRequestModeEnabled()) {
      return false;
    }
    return true;
  }

  getGroupPermission() {
    return 'ROLEGUARANTEEROLE';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('guaranteeRole.name', 'asc');
  }
}
