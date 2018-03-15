import EntityManager from './EntityManager';
import { WorkflowHistoricProcessInstanceService, LocalizationService} from '../../services';
import * as Utils from '../../utils';

export default class WorkflowHistoricProcessInstanceManager extends EntityManager {

  constructor() {
    super();
    this.service = new WorkflowHistoricProcessInstanceService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'WorkflowHistoricProcessInstance';
  }

  getCollectionType() {
    // Use in the version 8.x.x return 'workflowHistoricProcessInstances';
    return 'resources';
  }

  /**
   * Localization the given workflow process, by given attribute (name).
   *
   * @param  {[type]} task
   * @param  {String} [attribute='name']
   * @return {[String]}
   */
  localize(process, attribute = 'name') {
    if (!process) {
      return null;
    }
    const name = process[attribute];
    if (name) {
      const params = Utils.Ui.parseLocalizationParams(name);
      return LocalizationService.i18n(`wf.${process.processDefinitionKey}.${attribute}`, params);
    }
  }
}
