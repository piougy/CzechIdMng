

import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';

class WorkflowProcessInstanceService extends AbstractService {

  getApiPath(){
    return '/workflow/processes';
  }

  getNiceLabel(proc){
    if (!proc) {
      return '';
    }
    return proc.processDefinitionName;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('processDefinitionName');
  }
}

export default WorkflowProcessInstanceService;
