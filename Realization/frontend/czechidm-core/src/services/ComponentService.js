import _ from 'lodash';
//
import ComponentLoader from '../utils/ComponentLoader';

/**
* Service for load module components
*
* @author Radek Tomiška
*/
export default class ComponentService {

  getComponentDescriptor(moduleId) {
    return ComponentLoader.getComponentDescriptor(moduleId);
  }

  /**
   * Find component by id. Search in all components (defined in component-descriptor) and all modules
   *
   * @param  {string} componentId
   * @return {object} Component
   */
  getComponent(componentId) {
    return ComponentLoader.getComponent(componentId);
  }

  /**
   * Find component definition by id. Search in all components (defined in component-descriptor) and all modules
   *
   * @param  {string} componentId
   * @return {object} Component definition
   */
  getComponentDefinition(componentId) {
    return ComponentLoader.getComponentDefinition(componentId);
  }

  /**
   * Finds component definitions by the given type
   *
   * @param  {string} componentType
   * @return {immutable.map} immutable map of component definitions with the given type, key is component id
   */
  getComponentDefinitions(componentType) {
    return ComponentLoader.getComponentDefinitions(componentType);
  }

  /**
   * Returns registered component by type and entity type
   *
   * @param  {string} type       e.g. ENTITY_INFO_COMPONENT_TYPE
   * @param  {string} entityType e.g. identity
   * @return {object}            component
   */
  getComponentByEntityType(type, entityType) {
    if (!type || !entityType) {
      LOGGER.warn('[ComponentService] Compontent type and antity type is required');
      return null;
    }
    //
    return this.getComponentDefinitions(type).find(component => {
      if (!component.entityType) {
        return false;
      }
      // multiple types
      if (_.isArray(component.entityType)) {
        for (const entityTypeItem of component.entityType) {
          if (entityTypeItem.toLowerCase() === entityType.toLowerCase()) {
            return true;
          }
        }
        return false;
      }
      // single value
      return component.entityType.toLowerCase() === entityType.toLowerCase();
    });
  }

  /**
   * Returns registered EntityInfo component by type and entity type
   *
   * @param  {string} entityType e.g. identity
   * @return {object}            component
   */
  getEntityInfoComponent(entityType) {
    return this.getComponentByEntityType(ComponentService.ENTITY_INFO_COMPONENT_TYPE, entityType);
  }

  /**
   * Returns registered SelectBox component by type and entity type
   *
   * @param  {string} entityType e.g. identity
   * @return {object}            component
   */
  getEntitySelectBoxComponent(entityType) {
    return this.getComponentByEntityType(ComponentService.ENTITY_SELECT_BOX_COMPONENT_TYPE, entityType);
  }
}
// reserved component types
ComponentService.ENTITY_INFO_COMPONENT_TYPE = 'entity-info';
ComponentService.DASHBOARD_COMPONENT_TYPE = 'dashboard';
ComponentService.FORM_ATTRIBUTE_RENDERER = 'form-attribute-renderer';
ComponentService.ENTITY_SELECT_BOX_COMPONENT_TYPE = 'entity-select-box';
