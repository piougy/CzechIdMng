import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Password metadata.
 *
 * @author Ondřej Kopr
 * @since 9.6.0
 */
export default class PasswordService extends AbstractService {

  getApiPath() {
    return '/passwords';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  // dto
  supportsPatch() {
    return false;
  }

  supportsAuthorization() {
    return true;
  }

  /**
   * Returns default searchParameters for passwords
   *
   * @return {SearchParameters} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created');
  }
}
