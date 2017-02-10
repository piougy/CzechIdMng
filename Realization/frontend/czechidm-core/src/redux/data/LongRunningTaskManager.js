import EntityManager from './EntityManager';
import { LongRunningTaskService } from '../../services';

/**
 * Long tunning task administration
 *
 * @author Radek Tomiška
 */
export default class LongRunningTaskManager extends EntityManager {

  constructor() {
    super();
    this.service = new LongRunningTaskService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'LongRunningTask';
  }

  getCollectionType() {
    return 'longRunningTasks';
  }

  /**
   * Returns nice long running counter / count
   *
   * @param  {object} entity
   * @return {string}
   */
  getProcessedCount(entity) {
    if (!entity || (entity.counter === null && entity.count === null)) {
      return null;
    }
    return `${entity.counter !== null ? entity.counter : '?'} / ${entity.count !== null ? entity.count : '?'}`;
  }

  /**
   * Cancel given task manually
   *
   * @param {object} task
   * @param {string} uiKey
   * @param {func} callback
   * @return {action}
   */
  cancel(task, uiKey, cb) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().cancel(task)
        .then(() => {
          dispatch(this.dataManager.stopRequest(uiKey));
          if (cb) {
            cb();
          }
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.dataManager.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * Interrupt given task manually
   *
   * @param {object} task
   * @param {string} uiKey
   * @param {func} callback
   * @return {action}
   */
  interrupt(task, uiKey, cb) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().interrupt(task)
        .then(() => {
          dispatch(this.dataManager.stopRequest(uiKey));
          if (cb) {
            cb();
          }
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.dataManager.receiveError(null, uiKey, error));
        });
    };
  }
}
