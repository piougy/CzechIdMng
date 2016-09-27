import Immutable from 'immutable';
import _ from 'lodash';

let _moduleDescriptors = new Immutable.Map();

export default class ModuleLoader {

  static init(moduleDescriptors) {
    // descriptor is simple json and dont need to be initialized
    // we need all descriptors loaded it start
    _moduleDescriptors = moduleDescriptors;
  }

  static getModuleDescriptor(moduleId) {
    return _moduleDescriptors.get(moduleId);
  }

  static getModuleDescriptors() {
    return _moduleDescriptors.toArray();
  }

  static enable(moduleId, enabled = true) {
    const moduleDescriptor = ModuleLoader.getModuleDescriptor(moduleId);
    _moduleDescriptors = _moduleDescriptors.set(moduleId, _.merge({}, moduleDescriptor, { enabled }));
  }

  /**
   * Returns ids of all enabled modules
   * TODO: use static config.json - some modules can be used as internal dependency
   */
  static getEnabledModuleIds() {
    const enabledModuleIds = [];
    ModuleLoader.getModuleDescriptors().forEach(moduleDescriptor => {
      if (moduleDescriptor.enabled === undefined || moduleDescriptor.enabled === null || moduleDescriptor.enabled !== false) {
        enabledModuleIds.push(moduleDescriptor.id);
      }
    });
    return enabledModuleIds;
  }
}
