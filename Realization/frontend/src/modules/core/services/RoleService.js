'use strict';

import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class RoleService extends AbstractService {

  getApiPath(){
    return '/roles';
  }

  getNiceLabel(role){
    if (!role) {
      return '';
    }
    return role.name;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }
}

export default RoleService;
