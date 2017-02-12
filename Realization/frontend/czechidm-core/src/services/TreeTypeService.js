import RestApiService from './RestApiService';
import AbstractService from './AbstractService';
import * as Utils from '../utils';
import SearchParameters from '../domain/SearchParameters';

class TreeTypeService extends AbstractService {

  const
  getApiPath() {
    return '/tree-types';
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
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }

  /**
   * Returns dafult tree Type
   *
   * @return {promise}
   */
  getDefaultTreeType() {
    return RestApiService
    .get(this.getApiPath() + '/search/default')
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

export default TreeTypeService;
