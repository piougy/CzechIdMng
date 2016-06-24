import EntityManager from '../../../../modules/core/redux/data/EntityManager';
import { VpnApprovalTaskService } from '../../services';
import ApprovalTaskManager from '../../../../redux/data/ApprovalTaskManager';

const service = new VpnApprovalTaskService();

/**
 * Manager for vpn records
 */
export default class VpnApprovalTaskManager extends ApprovalTaskManager {

  getService() {
    return service;
  }

  getEntityType() {
    return 'VpnApprovalTask';
  }
}
