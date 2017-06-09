import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class NotificationRecipientService extends AbstractService {

  const
  getApiPath() {
    return '/notification-recipients';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    if (entity.module) {
      return `${entity.name} (${entity.module})`;
    }
    return entity.name;
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
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK);
  }
}

export default NotificationRecipientService;
