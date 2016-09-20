import ComponentLoader from '../utils/ComponentLoader';

/**
* Service for load module components
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
   * @return {array[object]} array of component definitions with the given type
   */
  getComponentDefinitions(componentType) {
    return ComponentLoader.getComponentDefinitions(componentType);
  }
}
