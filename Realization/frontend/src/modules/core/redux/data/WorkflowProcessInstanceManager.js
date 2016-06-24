'use strict'

import EntityManager from './EntityManager';
import { WorkflowProcessInstanceService } from '../../services';

export default class WorkflowProcessInstanceManager extends EntityManager {

  constructor () {
    super();
    this.service = new WorkflowProcessInstanceService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'WorkflowProcessInstance';
  }

  getCollectionType() {
    return 'resources';
  }
}
