import EntityManager from './EntityManager';
import { FormDefinitionService } from '../../services';

export default class FormDefinitionManager extends EntityManager {

  constructor() {
    super();
    this.service = new FormDefinitionService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'FormDefinition';
  }

  getCollectionType() {
    return 'formDefinitions';
  }

  /**
   * Return search paramaters for endpoind with information about form definition types.
   */
  getDefinitionTypesSearchParameters() {
    return this.getService().getDefinitionTypesSearchParameters();
  }
}
