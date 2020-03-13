import AbstractRequestService from './AbstractRequestService';
import SearchParameters from '../domain/SearchParameters';
import {i18n} from './LocalizationService';

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
    if (!entity || !entity._embedded || !entity._embedded.formAttribute) {
      return i18n('entity.RoleFormAttribute._type');
    }
    return entity._embedded.formAttribute.code;
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
