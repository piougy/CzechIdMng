import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class ScriptService extends AbstractService {

  getApiPath() {
    return '/scripts';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  // dto
  supportsPatch() {
    return false;
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
