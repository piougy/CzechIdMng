import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import SearchParameters from '../domain/SearchParameters';
import FormDefinitionService from './FormDefinitionService';
import * as Utils from '../utils';

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

  /**
   * Loads all registered attribute renderers.
   *
   * @return {promise}
   * @since 10.8.0
   */
  getSupportedAttributeRenderers() {
    return RestApiService
      .get(`${ this.getApiPath() }/search/supported-attribute-renderers`)
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

export default FormAttributeService;
