import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import IdentityService from './IdentityService';
import RoleService from './RoleService';

const identityService = new IdentityService();
const roleService = new RoleService();

/**
 * Role guarantees
 *
 * @author Radek Tomiška
 */
export default class RoleGuaranteeService extends AbstractService {

  getApiPath() {
    return '/role-guarantees';
  }

  getNiceLabel(entity) {
    if (!entity || !entity._embedded) {
      return '';
    }
    let label = `${roleService.getNiceLabel(entity._embedded.role)}`;
    if (entity.guarantee) {
      label += ` - ${identityService.getNiceLabel(entity._embedded.guarantee)}`;
    }
    //
    return label;
  }

  supportsPatch() {
    return true;
  }

  getGroupPermission() {
    return 'ROLEGUARANTEE';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('guarantee.username', 'asc');
  }
}
