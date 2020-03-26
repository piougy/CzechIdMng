import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
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
}

export default FormProjectionService;
