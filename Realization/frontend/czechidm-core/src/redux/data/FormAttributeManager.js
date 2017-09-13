import EntityManager from './EntityManager';
import { FormAttributeService } from '../../services';
import ComponentService from '../../services/ComponentService';

/**
 * Eav form attributes
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
export default class FormAttributeManager extends EntityManager {

  constructor() {
    super();
    this.service = new FormAttributeService();
    this.componentService = new ComponentService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'FormAttribute';
  }

  getCollectionType() {
    return 'formAttributes';
  }

  /**
   * Returns form value component by form attribute's face and persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {object} component or null
   */
  getFormComponent(attribute) {
    if (!attribute) {
      return null;
    }
    //
    return this.componentService.getComponentDefinitions(ComponentService.FORM_VALUE).find(component => {
      if (!component.persistentType) {
        // persistent type is required
        return false;
      }
      // component and attribute face type can be empty => default face by persistent type is used
      const _attributeFaceType = attribute.faceType || attribute.persistentType;
      const _componentFaceType = component.faceType || component.persistentType;
      // persistent and face type has to fit
      return attribute.persistentType.toLowerCase() === component.persistentType.toLowerCase()
          && _attributeFaceType.toLowerCase() === _componentFaceType.toLowerCase();
    });
  }
}
