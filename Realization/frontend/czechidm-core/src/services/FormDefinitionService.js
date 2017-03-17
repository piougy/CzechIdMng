import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

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
}

/**
 * Search all types for form definition
 */
FormDefinitionService.TYPES_SEARCH = 'types';

export default FormDefinitionService;
