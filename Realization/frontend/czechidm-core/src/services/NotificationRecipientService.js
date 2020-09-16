import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Notification recipient service.
 *
 * @author Radek Tomiška
 * @author Peter Šourek
 */
export default class NotificationRecipientService extends AbstractService {

  getApiPath() {
    return '/notification-recipients';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.realRecipient;
  }

  supportsPatch() {
    return false;
  }

  /**
   * Returns default searchParameters for notification recipients
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('realRecipient');
  }
}
