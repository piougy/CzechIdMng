'use strict';

import Immutable from 'immutable';
import ComponentLoader from '../modules/ComponentLoader';

const componentLoader = new ComponentLoader();

/**
* Service for load module components
*/
export default class ComponentService {

  constructor() {
  }

  getComponentDescriptor(moduleName) {
    return componentLoader.getComponentDescriptor(moduleName);
  }

  /**
   * Find component by name. Search in all components (defined in component-descriptor) and all modules
   * @param  {string} componentName
   * @return {object} Component
   */
  getComponent(componentName) {
    return componentLoader.getComponent(componentName);
  }

  getComponentDefinition(componentName) {
    return componentLoader.getComponentDefinition(componentName);
  }

  getComponentDefinitions(componentType) {
    return componentLoader.getComponentDefinitions(componentType);
  }
}
