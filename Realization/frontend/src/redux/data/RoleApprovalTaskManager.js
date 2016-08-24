import { RoleApprovalTaskService } from '../../services';
import ApprovalTaskManager from './ApprovalTaskManager';

const service = new RoleApprovalTaskService();

export default class RoleApprovalTaskManager extends ApprovalTaskManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'RoleApprovalTask';
  }
}
