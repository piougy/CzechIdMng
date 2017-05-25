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
    if (entity.module) {
      return `${entity.name} (${entity.module})`;
    }
    return entity.name;
  }

  supportsPatch() {
    return false;
  }

  /**
   * Returns default searchParameters for notification templates
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('code');
  }
}

export default NotificationTemplateService;
