import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Code list endpoint
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export default class CodeListService extends AbstractService {

  /**
   * Using in the request
   */
  getApiPath() {
    return '/code-lists';
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
      return entity.name;
    }
    return `${entity.name} (${code})`;
  }

  supportsAuthorization() {
    return true;
  }

  supportsBulkAction() {
    return true;
  }

  getGroupPermission() {
    return 'CODELIST';
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
