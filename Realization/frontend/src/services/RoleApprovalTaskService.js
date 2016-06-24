'use strict';

import { AbstractService, RestApiService } from '../modules/core/services/';

class RoleApprovalTaskService extends AbstractService {

  getApiPath() {
    return '/idm/seam/resource/api-v1/approval-tasks/role';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    let defaultSearchParameters = super.getDefaultSearchParameters();
    defaultSearchParameters.sort = [
      {
        field: 'createdDate',
        order: 'DESC'
      }
    ];
    return defaultSearchParameters;
  }

  /**
   * Approve task with this idTask
   * @param  {string} idTask
   * @param  {object} task   Task with new values (filled by user)
   * @return {Promise} task
   */
  approve(idTask, task) {
    return RestApiService.put(this.getApiPath() + `/`+idTask+`/approve`, task);
  }

  /**
   * Dissapprove task with this idTask
   * @param  {string} idTask
   * @param  {object} task   Task with new values (filled by user)
   * @return {Promise} task
   */
  disapprove(idTask, task) {
    return RestApiService.put(this.getApiPath() + `/`+idTask+`/disapprove`, task);
  }

  /**
   * All approvers for task with this idTask
   * @param  {string} idTask
   * @return {Promise}   Return list of identities (are trimmed, contain only username)
   */
  getApprovers(idTask) {
    return RestApiService.get(this.getApiPath() + `/`+idTask+`/approvers`);
  }

  getNiceLabel(entity) {
    if (entity) {
      return (entity.taskName ? entity.taskName : entity.id);
    }
    return '-';
  }

}

export default RoleApprovalTaskService;
