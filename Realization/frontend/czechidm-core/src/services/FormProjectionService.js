import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

/**
 * Form projections.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
class FormProjectionService extends AbstractService {

  getApiPath() {
    return '/form-projections';
  }

  getNiceLabel(entity, showOwnerType = true) {
    if (!entity) {
      return '';
    }
    let label = entity.code;
    if (showOwnerType) {
      label += ` - ${ Utils.Ui.getSimpleJavaType(entity.ownerType) }`;
    }
    //
    return label;
  }

  getGroupPermission() {
    return 'FORMPROJECTION';
  }

  supportsPatch() {
    return true;
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
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('code', 'asc');
  }

  /**
   * Loads all registered routes (available for authorization policies)
   *
   * @return {promise}
   * @since 10.3.0
   */
  getSupportedRoutes() {
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

export default FormProjectionService;
