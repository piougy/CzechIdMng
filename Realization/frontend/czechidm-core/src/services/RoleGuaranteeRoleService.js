import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RoleService from './RoleService';

const roleService = new RoleService();

/**
 * Role guarantees - by role
 *
 * @author Radek Tomiška
 */
export default class RoleGuaranteeRoleService extends AbstractService {

  getApiPath() {
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
