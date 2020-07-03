import EntityManager from './EntityManager';
import { LongRunningTaskService } from '../../services';
import FormAttributeManager from './FormAttributeManager';
import * as Utils from '../../utils';

const formAttributeManager = new FormAttributeManager();


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

  getNiceLabel(entity, supportedTasks = null) {
    let _taskType;
    if (supportedTasks && supportedTasks.has(entity.taskType)) {
      _taskType = supportedTasks.get(entity.taskType);
    } else if (entity.taskProperties) {
      const bulkAction = entity.taskProperties['core:bulkAction'];
      // try to find form attributes from form definition
      if (bulkAction) {
        _taskType = {
          formDefinition: {
            code: bulkAction.name,
            module: bulkAction.module,
            type: 'bulk-action'
          }
        };
      }
    }
    if (_taskType && _taskType.formDefinition) {
      const simpleTaskType = Utils.Ui.getSimpleJavaType(entity.taskType);
      let _label = formAttributeManager.getLocalization(_taskType.formDefinition, null, 'label', simpleTaskType);
      if (_label !== simpleTaskType) {
        _label += ` (${ simpleTaskType })`;
      }
      return _label;
    }
    return this.getService().getNiceLabel(entity);
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
          dispatch(this.deletedEntity(task.id, task, uiKey, cb));
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.dataManager.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * Executes prepared task from long running task queue
   *
   * @param {string} uiKey
   * @param {func} callback
   * @return {action}
   */
  processCreated(uiKey, cb) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().processCreated()
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

  processCreatedTask(taskId, uiKey, cb) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().processCreatedTask(taskId)
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
   * Run given task again.
   *
   * @param  {string}   taskId
   * @param  {string}   uiKey
   * @param  {Function} cb
   * @return {action}
   * @since 10.2.0
   */
  recover(taskId, uiKey, cb) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().recover(taskId)
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
   * Cancel entities - bulk action
   *
   * @param  {array[object]} entities - Entities to cancel
   * @param  {string} uiKey = null - ui key for loading indicator etc
   * @param  {func} cb - function will be called after entities are canceled or error occured
   * @return {object} - action
   */
  cancelEntities(entities, uiKey = null, cb = null) {
    return (dispatch) => {
      dispatch(
        this.startBulkAction(
          {
            name: 'cancel',
            title: this.i18n(`action.cancel.header`, { count: entities.length })
          },
          entities.length
        )
      );
      const successEntities = [];
      const approveEntities = [];
      let currentEntity = null; // currentEntity in loop
      entities.reduce((sequence, entity) => {
        return sequence.then(() => {
          // stops when first error occurs
          currentEntity = entity;
          return this.getService().cancel(entity);
        }).then(() => {
          dispatch(this.updateBulkAction());
          successEntities.push(entity);
        }).catch(error => {
          if (error && error.statusCode === 202) {
            dispatch(this.updateBulkAction());
            approveEntities.push(entity);
          } else {
            if (currentEntity.id === entity.id) { // we want show message for entity, when loop stops
              if (!cb) { // if no callback given, we need show error
                dispatch(this.flashMessagesManager.addErrorMessage({
                  title: this.i18n(`action.cancel.error`, { record: this.getNiceLabel(entity) })
                }, error));
              } else { // otherwise caller has to show eror etc. himself
                cb(entity, error, null);
              }
            }
            throw error;
          }
        });
      }, Promise.resolve())
        .catch((error) => {
          // nothing - message is propagated before
          // catch is before then - we want execute next then clausule
          return error;
        })
        .then((error) => {
          if (successEntities.length > 0) {
            dispatch(this.flashMessagesManager.addMessage({
              level: 'success',
              message: this.i18n(`action.cancel.success`, {
                count: successEntities.length,
                records: this.getNiceLabels(successEntities).join(', '),
                record: this.getNiceLabel(successEntities[0])
              })
            }));
          }
          if (approveEntities.length > 0) {
            dispatch(this.flashMessagesManager.addMessage({
              level: 'info',
              message: this.i18n(`action.cancel.accepted`, {
                count: approveEntities.length,
                records: this.getNiceLabels(approveEntities).join(', '),
                record: this.getNiceLabel(approveEntities[0])
              })
            }));
          }
          dispatch(this.stopBulkAction());
          if (cb) {
            cb(null, error, successEntities);
          }
        });
    };
  }
}
