import EntityManager from './EntityManager';
import { WorkflowHistoricTaskInstanceService } from '../../services';

export default class WorkflowHistoricTaskInstanceManager extends EntityManager {

  constructor() {
    super();
    this.service = new WorkflowHistoricTaskInstanceService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'WorkflowHistoricTaskInstance';
  }

  getCollectionType() {
    // Use in the version 8.x.x return 'workflowHistoricTaskInstances';
    return 'resources';
  }
}
