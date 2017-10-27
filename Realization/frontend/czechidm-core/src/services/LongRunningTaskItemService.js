import RestApiService from './RestApiService';
import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import SchedulerService from './SchedulerService';
import * as Utils from '../utils';

/**
 * Processed task item administration
 *
 * @author Marek Klement
 */
export default class LongRunningTaskItemService extends AbstractService {

  constructor() {
    super();
    this.schedulerService = new SchedulerService();
  }

  getApiPath() {
    return '/long-running-task-item';
  }

  getNiceLabel(entity) {
    if (entity && entity.id) {
      return entity.id;
    }
    return '';
  }

  /**
   * Returns default searchParameters for scripts
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }

  addToQueue(entityId, scheduledTask) {
    return RestApiService
    .post(this.getApiPath() + `/${entityId}/queue-item`, scheduledTask)
    .then(response => {
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
