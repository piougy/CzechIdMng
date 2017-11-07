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
}
// reserved component types
ComponentService.ENTITY_INFO_COMPONENT_TYPE = 'entity-info';
ComponentService.DASHBOARD_COMPONENT_TYPE = 'dashboard';
ComponentService.FORM_ATTRIBUTE_RENDERER = 'form-attribute-renderer';
ComponentService.ENTITY_SELECT_BOX_COMPONENT_TYPE = 'entity-select-box';
