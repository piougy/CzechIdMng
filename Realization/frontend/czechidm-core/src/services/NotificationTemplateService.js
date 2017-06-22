import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

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

  notificationOperationById(id, operation) {
    return RestApiService
      .get(this.getApiPath() + `/${encodeURIComponent(id)}/${operation}`)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        if (Utils.Response.hasInfo(json)) {
          throw Utils.Response.getFirstInfo(json);
        }
        return json;
      }).catch((error) => {
        if (error && error.statusEnum === 'NOTIFICATION_TEMPLATE_XML_FILE_NOT_FOUND') {
          return error;
        }
        throw error;
      });
  }
}

export default NotificationTemplateService;
