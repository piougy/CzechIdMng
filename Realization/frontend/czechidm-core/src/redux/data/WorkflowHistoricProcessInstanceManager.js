import EntityManager from './EntityManager';
import { WorkflowHistoricProcessInstanceService } from '../../services';

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
    return 'workflowHistoricProcessInstances';
  }
}
