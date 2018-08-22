import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RoleService from './RoleService';

const roleService = new RoleService();

/**
 * Role composition - define busimess role
 *
 * @author Radek Tomiška
 */
export default class RoleCompositionService extends AbstractService {

  getApiPath() {
    return '/role-compositions';
  }

  getNiceLabel(entity) {
    if (!entity || !entity._embedded) {
      return '';
    }
    let label = `${roleService.getNiceLabel(entity._embedded.superior)}`;
    label += ` - ${roleService.getNiceLabel(entity._embedded.sub)}`;
    //
    return label;
  }

  supportsPatch() {
    return true;
  }

  getGroupPermission() {
    return 'ROLECOMPOSITION';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('superior.name', 'asc').setSort('sub.name', 'asc');
  }
}
