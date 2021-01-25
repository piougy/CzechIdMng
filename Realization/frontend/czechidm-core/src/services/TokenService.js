import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Tokens.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
class TokenService extends AbstractService {

  getApiPath() {
    return '/tokens';
  }

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'TOKEN';
  }

  supportsPatch() {
    return true;
  }

  supportsBulkAction() {
    return true;
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${ entity.tokenType }`;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('issuedAt', 'desc');
  }
}

export default TokenService;
