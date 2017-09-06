import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Eav form attributes
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
class FormAttributeService extends AbstractService {

  getApiPath() {
    return '/form-attributes';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.name}`;
  }

  getGroupPermission() {
    return 'FORMATTRIBUTE';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('seq', 'asc').setSort('code', 'asc');
  }
}

export default FormAttributeService;
