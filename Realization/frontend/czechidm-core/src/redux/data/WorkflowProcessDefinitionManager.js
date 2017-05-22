import EntityManager from './EntityManager';
import { WorkflowProcessDefinitionService } from '../../services';

export default class WorkflowProcessDefinitionManager extends EntityManager {

  constructor() {
    super();
    this.service = new WorkflowProcessDefinitionService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'WorkflowDefinitionInstance';
  }

  getCollectionType() {
    return 'workflowProcessDefinitions';
  }
}
