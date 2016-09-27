import Immutable from 'immutable';

let _components = new Immutable.Map();
let _componentDescriptors = new Immutable.Map();

export default class ComponentLoader {

  static initComponents(componentDescriptors) {
    _componentDescriptors = componentDescriptors;
    _componentDescriptors.toArray().map(descriptor => {
      this._fillComponents(descriptor);
    });
  }

  static getComponentDescriptor(moduleName) {
    return _componentDescriptors.get(moduleName);
  }

  static getComponent(componentId) {
    if (_components.get(componentId)) {
      return _components.get(componentId).component;
    }
    return null;
  }

  static getComponentDefinition(componentId) {
    return _components
      .find(component => {
        return component.id === componentId;
      });
  }

  static getComponentDefinitions(type) {
    return _components
      .filter(component => {
        return !component.disabled && (!type || component.type === type);
      })
      .sortBy(item => item.order);
  }

  static _fillComponents(componentDescriptor) {
    for (const component of componentDescriptor.components) {
      if (!_components.has(component.id) || (_components.get(component.id).priority || 0) < (component.priority || 0)) {
        component.module = componentDescriptor.id;
        _components = _components.set(component.id, component);
      }
    }
  }
}
