import Immutable from 'immutable';
import { componentDescriptors } from '../../components';

export default class ComponentLoader {

  constructor() {
    this._components = new Immutable.Map();
    this._componentDescriptors = componentDescriptors;
    this._componentDescriptors.toArray().map(descriptor => {
      this._fillComponents(descriptor);
    });
  }

  getComponentDescriptor(moduleName) {
    return componentDescriptors.get(moduleName);
  }

  getComponent(componentId) {
    if (this._components.get(componentId)) {
      return this._components.get(componentId).component;
    }
    return null;
  }

  getComponentDefinition(componentId) {
    return this._components
      .find(component => {
        return component.id === componentId;
      });
  }

  getComponentDefinitions(type) {
    return this._components
      .filter(component => {
        return !component.disabled && (!type || component.type === type);
      })
      .sortBy(item => item.order);
  }

  _fillComponents(componentDescriptor) {
    for (const component of componentDescriptor.components) {
      if (!this._components.has(component.id) || (this._components.get(component.id).priority || 0) < (component.priority || 0)) {
        component.module = componentDescriptor.id;
        this._components = this._components.set(component.id, component);
      }
    }
  }
}
