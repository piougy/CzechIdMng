'use strict';

import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';

class WorkflowHistoricProcessInstanceService extends AbstractService {

  getApiPath(){
    return '/workflow/history/processes';
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
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('startTime', false);
  }
}

export default WorkflowHistoricProcessInstanceService;
