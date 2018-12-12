import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import LocalizationService from './LocalizationService';

/**
 * Code list items endpoint
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export default class CodeListItemService extends AbstractService {

  /**
   * Using in the request
   */
  getApiPath() {
    return '/code-list-items';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    let code = null;
    if (entity.name !== entity.code) {
      code = entity.code;
    }
    if (!code) {
      return LocalizationService.i18n(entity.name);
    }
    return `${LocalizationService.i18n(entity.name)} (${code})`;
  }

  supportsAuthorization() {
    return true;
  }

  supportsBulkAction() {
    return true;
  }

  getGroupPermission() {
    return 'CODELISTITEM';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }
}
