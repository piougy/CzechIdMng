import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class RuleService extends AbstractService {

  const
  getApiPath() {
    return '/rules';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  /**
   * Returns default searchParameters for rules
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }
}

export default RuleService;
