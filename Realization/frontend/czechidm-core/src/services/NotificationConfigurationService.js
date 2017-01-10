import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

export default class NotificationService extends AbstractService {

  getApiPath() {
    return '/notification-configurations';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.topic} - ${entity.notificationType}`;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('topic');
  }
}
