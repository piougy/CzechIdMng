

import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';

class WorkflowTaskInstanceService extends AbstractService {

  getApiPath() {
    return '/workflow-tasks';
  }

  getNiceLabel(task) {
    if (!task) {
      return '';
    }
    return task.taskName;
  }

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'WORKFLOWTASK';
  }

  completeTask(id, formData) {
    return RestApiService.put(this.getApiPath() + `/${id}/complete`, formData);
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('taskName');
  }
}

export default WorkflowTaskInstanceService;
