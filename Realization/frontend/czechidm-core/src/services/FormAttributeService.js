import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class FormAttributeService extends AbstractService {

  getApiPath() {
    return '/form-attributes';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.name}`;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }
}

export default FormAttributeService;
