import AbstractService from './AbstractService';
import SearchParameters from 'core/domain/SearchParameters';

export default class ConfigurationService extends AbstractService {

  getApiPath() {
    return '/configurations';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name', 'asc');
  }
}
