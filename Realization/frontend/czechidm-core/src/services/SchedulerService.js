import RestApiService from './RestApiService';
import AbstractService from './AbstractService';
import * as Utils from '../utils';

/**
 * Scheduler administration
 *
 * @author Radek Tomiška
 */
export default class SchedulerService extends AbstractService {

  getApiPath() {
    return '/scheduler-tasks';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return this.getSimpleTaskType(entity.taskType);
  }

  /**
   * Return simple class name
   *
   * @param  {string} taskType cannonical class name
   * @return {string}
   */
  getSimpleTaskType(taskType) {
    if (!taskType) {
      return null;
    }
    return taskType.split('.').pop(-1);
  }

  /**
   * Loads all registered tasks (available for scheduling)
   *
   * @return {promise}
   */
  getSupportedTasks() {
    return RestApiService
    .get(this.getApiPath() + '/search/supported')
    .then(response => {
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
   * Loads scheduled tasks
   *
   * @return {promise}
   */
  getTasks() {
    return RestApiService
    .get(this.getApiPath())
    .then(response => {
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
   * Run given task manually
   *
   * @param  {string} taskId
   * @return {promise}
   */
  runTask(taskId) {
    return RestApiService
    .post(this.getApiPath() + `/${taskId}/run`)
    .then(response => {
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
   * Deletes given trigger
   *
   * @param  {string} taskId
   * @param  {string} triggerId
   * @return {promise}
   */
  deleteTrigger(trigger) {
    return RestApiService
    .delete(this.getApiPath() + `/${trigger.taskId}/triggers/${trigger.id}`)
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
   * Creates given trigger
   *
   * @param  {string} taskId
   * @param  {string} trigger
   * @return {promise}
   */
  createTrigger(trigger) {
    const type = trigger.type === 'SIMPLE' ? 'simple' : 'cron';
    if (type === 'simple') {
      trigger._type = 'SimpleTaskTrigger';
    } else {
      trigger._type = 'CronTaskTrigger';
    }
    //
    return RestApiService
    .post(this.getApiPath() + `/${trigger.taskId}/triggers/${type}`, trigger)
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
