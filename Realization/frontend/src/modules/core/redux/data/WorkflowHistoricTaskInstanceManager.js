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
    return 'resources';
  }
}
