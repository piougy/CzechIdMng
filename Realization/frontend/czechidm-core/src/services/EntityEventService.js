import * as Utils from '../utils';
import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import SearchParameters from '../domain/SearchParameters';
import LocalizationService from './LocalizationService';
import moment from 'moment';

/**
 * Entity events and states
 *
 * @author Radek Tomiška
 */
export default class EntityEventService extends AbstractService {

  getApiPath() {
    return '/entity-events';
  }

  supportsBulkAction() {
    return true;
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${moment(entity.created).format(LocalizationService.i18n('format.date'))}: ${entity.eventType}`;
  }

  /**
   * Returns default searchParameters for scripts
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }

  /**
   * Delete all entity events.
   *
   * @return {Promise}
   */
  deleteAll() {
    return RestApiService
      .delete(RestApiService.getUrl(this.getApiPath() + `/action/bulk/delete`))
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        if (response.status === 204) {
          return {};
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }
}
