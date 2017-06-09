import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

export default class NotificationService extends AbstractService {

  getApiPath() {
    return '/notifications';
  }

  getNiceLabel(notification) {
    if (!notification) {
      return '';
    }
    return notification.message.subject;
  }

  supportsPatch() {
    return false;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }
}
