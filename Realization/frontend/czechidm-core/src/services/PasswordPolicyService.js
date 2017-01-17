import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class PasswordPolicyService extends AbstractService {

  const
  getApiPath() {
    return '/password-policies';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  /**
   * Returns default searchParameters for password policy
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }
}

export default PasswordPolicyService;
