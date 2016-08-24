import * as Utils from '../utils';
import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import SearchParameters from '../domain/SearchParameters';

class WorkflowProcessDefinitionService extends AbstractService {

  getApiPath() {
    return '/workflow/definitions';
  }

  getSearchQuickApiPatch() {
    return '/workflow/definitions/search/quick';
  }

  getNiceLabel(entity) {
    if (entity) {
      return (entity.name);
    }
    return '-';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }

  _getDefinitions(apiUrl) {
    return RestApiService
      .get(apiUrl + '/')
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Find current workflow definitions by search quick api
   */
  getSeachQuickDefinitions() {
    return this._getDefinitions(this.getSearchQuickApiPatch());
  }

  /**
   * Find all current Workflow definitions
   */
  getAllDefinitions() {
    return this._getDefinitions(this.getApiPath());
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

export default WorkflowProcessDefinitionService;
