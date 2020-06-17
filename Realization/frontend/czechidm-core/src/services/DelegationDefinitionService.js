import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

/**
 * Definition of a delegation service.
 *
 * @author Vít Švanda
 * @since 10.4.0
 */
class DelegationDefinitionService extends AbstractService {

  getApiPath() {
    return '/delegation-definitions';
  }

  getNiceLabel(entity) {
    if (!entity || entity._embedded || entity.embedded.identity) {
      return '';
    }
    const type = entity.type;
    let label = entity.embedded.identity.username;
    if (type) {
      label += ` - ${ type }`;
    }
    //
    return label;
  }

  getGroupPermission() {
    return 'DELEGATIONDEFINITION';
  }

  supportsPatch() {
    return false;
  }

  supportsBulkAction() {
    return true;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }

  /**
   * Loads all registered delegation types.
   *
   * @return {promise}
   */
  getSupportedTypes() {
    return RestApiService
      .get(`${ this.getApiPath() }/search/supported`)
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

export default DelegationDefinitionService;
