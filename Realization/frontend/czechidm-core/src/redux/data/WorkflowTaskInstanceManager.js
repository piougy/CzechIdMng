import EntityManager from './EntityManager';
import { WorkflowTaskInstanceService } from '../../services';
import * as Utils from '../../utils';

export default class WorkflowTaskInstanceManager extends EntityManager {

  constructor() {
    super();
    this.service = new WorkflowTaskInstanceService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'WorkflowTaskInstance';
  }

  getCollectionType() {
    return 'workflowTaskInstances';
  }

  completeTask(task, formData, uiKey = null, cb = null) {
    if (!task || !formData) {
      return null;
    }
    uiKey = this.resolveUiKey(uiKey, task.id);
    return (dispatch) => {
      dispatch(this.requestEntity(task.id, uiKey));
      this.getService().completeTask(task.id, formData)
      .then(response => {
        if (response.status === 200) {
          return null;
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      })
      .then(json => {
        if (json) {
          dispatch(this.receiveEntity(task.id, json, uiKey, cb));
        } else {
          cb(task, null);
        }
      })
      .catch(error => {
        dispatch(this.receiveError(task, uiKey, error, cb));
      });
    };
  }
}
