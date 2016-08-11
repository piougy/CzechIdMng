import EntityManager from '../../modules/core/redux/data/EntityManager';
import { ApprovalTaskService } from '../../services';
import SecurityManager from '../../modules/core/redux/security/SecurityManager';

const service = new ApprovalTaskService();

export default class ApprovalTaskManager extends EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  /**
  * Check if is logged user in approvers for this task
  * @param  {[array]}  approvers [array of users]
  * @return {Boolean}
  */
  hasEditRight(approvers) {
    if (SecurityManager.isAdmin()) {
      return true;
    }
    for (const approver of approvers) {
      if (SecurityManager.equalsAuthenticated(approver.name)) {
        return true;
      }
    }
    return false;
  }

  getEntityType() {
    return 'ApprovalTask';
  }
}
