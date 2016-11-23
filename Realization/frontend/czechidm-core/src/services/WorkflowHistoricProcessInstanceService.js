

import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';

class WorkflowHistoricProcessInstanceService extends AbstractService {

  getApiPath() {
    return '/workflow-history-processes';
  }

  getNiceLabel(proc) {
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

  /**
  * Generate and download diagram of process as PNG image
  */
  downloadDiagram(id, cb) {
    return RestApiService
    .download(this.getApiPath() + `/${id}/diagram`)
    .then(response => {
      if (response.status === 403) {
        throw new Error(403);
      }
      if (response.status === 404) {
        throw new Error(404);
      }
      return response.blob();
    })
    .then(blob => {
      cb(blob);
    });
  }
}

export default WorkflowHistoricProcessInstanceService;
