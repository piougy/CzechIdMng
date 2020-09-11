import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import { SchedulerService } from '../../services';
import DataManager from './DataManager';
import FormAttributeManager from './FormAttributeManager';
import * as Utils from '../../utils';

const formAttributeManager = new FormAttributeManager();

/**
 * Scheduler administration
 *
 * @author Radek Tomiška
 */
export default class SchedulerManager extends EntityManager {

  constructor() {
    super();
    this.dataManager = new DataManager();
    this.service = new SchedulerService();
  }

  getService() {
    return this.service;
  }

  getNiceLabel(entity, showDescription = true, supportedTasks = null) {
    let _taskType;
    if (supportedTasks && supportedTasks.has(entity.taskType)) {
      _taskType = supportedTasks.get(entity.taskType);
    }
    if (_taskType && _taskType.formDefinition) {
      const simpleTaskType = Utils.Ui.getSimpleJavaType(entity.taskType);
      let _label = formAttributeManager.getLocalization(_taskType.formDefinition, null, 'label', simpleTaskType);
      if (_label !== simpleTaskType) {
        _label += ` (${ simpleTaskType })`;
      }
      return _label;
    }
    return this.getService().getNiceLabel(entity, showDescription);
  }

  getEntityType() {
    return 'SchedulerTask';
  }

  getCollectionType() {
    return 'tasks';
  }

  /**
   * Return simple class name
   *
   * @param  {string} taskType cannonical class name
   * @return {string}
   */
  getSimpleTaskType(taskType) {
    return this.getService().getSimpleTaskType(taskType);
  }

  /**
   * Loads registeered scheduled tasks
   *
   * @return {action}
   */
  fetchSupportedTasks() {
    const uiKey = SchedulerManager.UI_KEY_SUPPORTED_TASKS;
    //
    return (dispatch, getState) => {
      const loaded = DataManager.getData(getState(), uiKey);
      const showLoading = DataManager.isShowLoading(getState(), uiKey);
      if (loaded || showLoading) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getSupportedTasks()
          .then(json => {
            let tasks = new Immutable.Map();
            if (json._embedded && json._embedded.tasks) {
              json._embedded.tasks.forEach(item => {
                tasks = tasks.set(item.taskType, item);
              });
            }
            dispatch(this.dataManager.receiveData(uiKey, tasks));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.dataManager.receiveError(null, uiKey, error));
          });
      }
    };
  }

  /**
   * Run given task manually
   *
   * @param  {string} taskId
   * @param  {func} callback
   * @return {action}
   */
  runTask(taskId, cb) {
    const uiKey = SchedulerManager.UI_KEY_TASKS;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().runTask(taskId)
        .then(() => {
          dispatch(this.dataManager.stopRequest(uiKey, null, cb));
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.dataManager.receiveError(null, uiKey, error));
        });
    };
  }

  dryRunTask(taskId, cb) {
    const uiKey = SchedulerManager.UI_KEY_TASKS;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().dryRunTask(taskId)
        .then(() => {
          dispatch(this.dataManager.stopRequest(uiKey, null, cb));
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.dataManager.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * Deletes given trigger
   *
   * @param  {string} taskId
   * @param  {func} callback
   * @return {action}
   */
  deleteTrigger(trigger, cb) {
    const uiKey = SchedulerManager.UI_KEY_TASKS;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().deleteTrigger(trigger)
        .then(() => {
          dispatch(this.dataManager.stopRequest(uiKey, null, cb));
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.dataManager.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * Deletes given trigger
   *
   * @param  {string} taskId
   * @param  {func} callback
   * @return {action}
   */
  createTrigger(trigger, cb) {
    const uiKey = SchedulerManager.UI_KEY_TASKS;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().createTrigger(trigger)
        .then(() => {
          dispatch(this.dataManager.stopRequest(uiKey, null, cb));
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.dataManager.receiveError(null, uiKey, error));
        });
    };
  }
}

SchedulerManager.UI_KEY_TASKS = 'scheduler-tasks';
SchedulerManager.UI_KEY_SUPPORTED_TASKS = 'scheduler-supported-tasks';
