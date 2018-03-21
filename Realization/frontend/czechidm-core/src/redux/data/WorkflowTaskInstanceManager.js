import EntityManager from './EntityManager';
import { WorkflowTaskInstanceService, LocalizationService } from '../../services';
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
    // Use in the version 8.x.x return 'workflowTaskInstances';
    return 'resources';
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

  /**
   * Localization the given workflow task, by given attribute (name or description).
   *
   * @param  {[type]} task
   * @param  {String} [attribute='name']
   * @return {[String]}
   */
  localize(task, attribute = 'name') {
    if (!task) {
      return null;
    }
    let name = null;
    if (attribute === 'name') {
      name = task.taskName;
    }
    if (attribute === 'description') {
      name = task.taskDescription;
    }
    if (name) {
      const params = Utils.Ui.parseLocalizationParams(name);
      const result = LocalizationService.i18n(`wf.${task.processDefinitionKey}.task.${task.definition.id}.${attribute}`, params);
      if (params.defaultValue === result) {
        // Result is exactly same as input ... it means localization was not found for this process key.
        // We try find localization in global
        return LocalizationService.i18n(`wf.task.${task.definition.id}.${attribute}`, params);
      }
      return result;
    }
  }
}
