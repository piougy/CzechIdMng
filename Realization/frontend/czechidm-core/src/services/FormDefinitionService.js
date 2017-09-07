import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

class FormDefinitionService extends AbstractService {

  getApiPath() {
    return '/form-definitions';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.name} (${entity.type})`;
  }

  getGroupPermission() {
    return 'FORMDEFINITION';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }

  getDefinitionTypesSearchParameters() {
    return super.getDefaultSearchParameters().setName(FormDefinitionService.TYPES_SEARCH);
  }

  getTypes() {
    return RestApiService.get(this.getApiPath() + `/search/types`)
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
}

/**
 * Search all types for form definition
 */
FormDefinitionService.TYPES_SEARCH = 'types';

export default FormDefinitionService;
