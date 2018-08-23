import AbstractRequestService from './AbstractRequestService';
import SearchParameters from '../domain/SearchParameters';
import RoleService from './RoleService';
import RoleCatalogueService from './RoleCatalogueService';

const roleService = new RoleService();
const roleCatalogueService = new RoleCatalogueService();

/**
 * Role catalogue - assigned Role
 *
 * @author Radek Tomiška
 */
export default class RoleCatalogueRoleService extends AbstractRequestService {

  /**
   * Using in the request
   */
  getSubApiPath() {
    return '/role-catalogue-roles';
  }

  getNiceLabel(entity) {
    if (!entity || !entity._embedded) {
      return '';
    }
    let label = `${roleService.getNiceLabel(entity._embedded.role)}`;
    if (entity.roleCatalogue) {
      label += ` - ${roleCatalogueService.getNiceLabel(entity._embedded.roleCatalogue)}`;
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
    return 'ROLECATALOGUEROLE';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('role.name', 'asc').setSort('roleCatalogue.name', 'asc');
  }
}
