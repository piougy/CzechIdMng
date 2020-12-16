import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import FormDefinitionService from './FormDefinitionService';

/**
 * Eav form attributes.
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
class FormAttributeService extends AbstractService {

  constructor() {
    super();
    this.formDefinitionService = new FormDefinitionService();
  }

  getApiPath() {
    return '/form-attributes';
  }

  getNiceLabel(entity, showDefinition = false) {
    if (!entity) {
      return '';
    }
    let label = '';
    if (entity.name === entity.code) {
      label = entity.name;
    } else {
      label = `${ entity.name } (${ entity.code })`;
    }
    if (showDefinition && entity._embedded && entity._embedded.formDefinition) {
      label += ` - ${ this.formDefinitionService.getNiceLabel(entity._embedded.formDefinition, false) }`;
    }
    //
    return label;
  }

  supportsBulkAction() {
    return true;
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
    return super.getDefaultSearchParameters()
      .setName(SearchParameters.NAME_QUICK)
      .setSize(100)
      .clearSort()
      .setSort('seq', 'asc')
      .setSort('code', 'asc');
  }
}

export default FormAttributeService;
