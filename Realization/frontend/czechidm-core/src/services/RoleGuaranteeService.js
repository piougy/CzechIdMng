import AbstractRequestService from './AbstractRequestService';
import SearchParameters from '../domain/SearchParameters';
import IdentityService from './IdentityService';
import RoleService from './RoleService';
import {i18n} from './LocalizationService';

const identityService = new IdentityService();
const roleService = new RoleService();

/**
 * Role guarantees
 *
 * @author Radek Tomiška
 */
export default class RoleGuaranteeService extends AbstractRequestService {

  /**
   * Using in the request
   */
  getSubApiPath() {
    return '/role-guarantees';
  }

  getNiceLabel(entity) {
    if (!entity || !entity._embedded || !entity._embedded.guarantee) {
      return i18n('entity.RoleGuarantee._type');
    }
    let label = `${roleService.getNiceLabel(entity._embedded.role)}`;
    if (entity.guarantee) {
      label += ` - ${identityService.getNiceLabel(entity._embedded.guarantee)}`;
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
