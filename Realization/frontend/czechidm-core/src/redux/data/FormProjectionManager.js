import EntityManager from './EntityManager';
import { FormProjectionService } from '../../services';
import FormDefinitionManager from './FormDefinitionManager';

/**
 * Form projections.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
export default class FormProjectionManager extends EntityManager {

  constructor() {
    super();
    this.service = new FormProjectionService();
    this.formDefinitionManager = new FormDefinitionManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'FormProjection';
  }

  getCollectionType() {
    return 'formProjections';
  }

  /**
   * Extended nice label.
   *
   * @param  {entity} entity
   * @param  {boolean} showOwnerType show owner type
   * @return {string}
   */
  getNiceLabel(entity, showOwnerType = true) {
    return this.getService().getNiceLabel(entity, showOwnerType);
  }

  /**
   * Projection property localization.
   *
   * @param  {object} projection
   * @param  {string} property
   * @param  {string} [defaultValue=null]
   * @return {string}
   */
  getLocalization(projection, property, defaultValue = null) {
    // reuse eav form feature
    return this.formDefinitionManager.getLocalization({ ...projection, type: 'form-projection' }, property, defaultValue);
  }
}
