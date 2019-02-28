import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Password history service
 *
 * @author Ondrej Kopr
 * @since 9.5.0
 */
export default class PasswordHistoryService extends AbstractService {

  getApiPath() {
    return '/password-histories';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.created} (${entity._embedded.identity.username})`;
  }

  supportsAuthorization() {
    return true;
  }

  supportsBulkAction() {
    return false;
  }

  getGroupPermission() {
    return 'AUDIT';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'DESC');
  }
}
