
import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class IdentityRoleService extends AbstractService {

  getApiPath() {
    return '/identity-roles';
  }

  /**
  * Returns default searchParameters for current entity type
  *
  * @return {object} searchParameters
  */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK);
  }
}

export default IdentityRoleService;
