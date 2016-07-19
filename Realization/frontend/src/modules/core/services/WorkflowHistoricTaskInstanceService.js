

import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

class WorkflowHistoricTaskInstanceService extends AbstractService {

  getApiPath(){
    return '/workflow/history/tasks';
  }

  getNiceLabel(proc){
    if (!proc) {
      return '';
    }
    return proc.name;
  }

  /**
  * Returns default searchParameters for current entity type
  *
  * @return {object} searchParameters
  */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('createTime', true);
  }
}

export default WorkflowHistoricTaskInstanceService;
