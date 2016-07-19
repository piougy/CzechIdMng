

import { RestApiService } from '../../../modules/core/services/';
import { ApprovalTaskService } from '../../../services/';

class VpnApprovalTaskService extends ApprovalTaskService {

  getApiPath() {
    return '/idm/seam/resource/api-v1/approval-tasks/vpn';
  }

  /**
   * VpnRecord for task with this idTask
   * @param  {string} idTask
   * @return {Promise}   Return VpnRecord
   */
  getVpnRecord(idTask) {
    return RestApiService.get(this.getApiPath() + `/`+idTask+`/vpn-record`);
  }
}

export default VpnApprovalTaskService;
