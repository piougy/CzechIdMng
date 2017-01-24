import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class ScriptService extends AbstractService {

  const
  getApiPath() {
    return '/scripts';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  /**
   * Returns default searchParameters for scripts
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }
}

export default ScriptService;
