import ComponentLoader from '../utils/ComponentLoader';

/**
* Service for load module components
*/
export default class ComponentService {

  constructor() {
    this.componentLoader = new ComponentLoader();
  }

  getComponentDescriptor(moduleId) {
    return this.componentLoader.getComponentDescriptor(moduleId);
  }

  /**
   * Find component by id. Search in all components (defined in component-descriptor) and all modules
   *
   * @param  {string} componentId
   * @return {object} Component
   */
  getComponent(componentId) {
    return this.componentLoader.getComponent(componentId);
  }

  /**
   * Find component definition by id. Search in all components (defined in component-descriptor) and all modules
   *
   * @param  {string} componentId
   * @return {object} Component definition
   */
  getComponentDefinition(componentId) {
    return this.componentLoader.getComponentDefinition(componentId);
  }

  /**
   * Finds component definitions by the given type
   *
   * @param  {string} componentType
   * @return {array[object]} array of component definitions with the given type
   */
  getComponentDefinitions(componentType) {
    return this.componentLoader.getComponentDefinitions(componentType);
  }
}
