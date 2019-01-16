import AbstractRequestService from './AbstractRequestService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Role form attribute (sub-definition)
 *
 * @author Vít Švanda
 */
export default class RoleFormAttributeService extends AbstractRequestService {

  /**
   * Using in the request
   */
  getSubApiPath() {
    return '/role-form-attributes';
  }

  getNiceLabel(entity) {
    if (!entity || !entity._embedded) {
      return '';
    }
    if (entity.formAttribute && entity._embedded.formAttribute) {
      return entity._embedded.formAttribute.code;
    }
  }

  supportsPatch() {
    return false;
  }

  getGroupPermission() {
    return 'ROLEFORMATTRIBUTE';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'asc');
  }
}
