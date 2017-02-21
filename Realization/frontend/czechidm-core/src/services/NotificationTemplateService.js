import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class NotificationTemplateService extends AbstractService {

  const
  getApiPath() {
    return '/notification-templates';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  /**
   * Returns default searchParameters for notification templates
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }
}

export default NotificationTemplateService;
