import RestApiService from './RestApiService';
import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import * as Utils from '../utils';
import SchedulerService from './SchedulerService';

/**
 * Long running task administration
 *
 * @author Radek Tomiška
 */
export default class LongRunningTaskService extends AbstractService {

  constructor() {
    super();
    this.schedulerService = new SchedulerService();
  }

  getApiPath() {
    return '/long-running-tasks';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return this.schedulerService.getSimpleTaskType(entity.taskType);
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
   * Cancel given task
   *
   * @param  {string} taskId
   * @return {promise}
   */
  cancel(task) {
    return RestApiService
    .put(this.getApiPath() + `/${task.id}/cancel`)
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

  /**
   * Intrerrupts given task
   *
   * @param  {string} taskId
   * @return {promise}
   */
  interrupt(task) {
    return RestApiService
    .put(this.getApiPath() + `/${task.id}/interrupt`)
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

  /**
   * Executes prepared task from long running task queue
   *
   * @return {promise}
   */
  processCreated() {
    return RestApiService
    .post(this.getApiPath() + `/action/process-created`)
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
