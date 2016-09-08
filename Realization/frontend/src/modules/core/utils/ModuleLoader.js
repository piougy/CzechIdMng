import { moduleDescriptors } from '../../modules';

export default class ModuleLoader {

  constructor() {
    // descriptor is simple json and dont need to be initialized
    // we need all descriptors loaded it start
    this._moduleDescriptors = moduleDescriptors;
  }

  getModuleDescriptor(moduleName) {
    return this._moduleDescriptors.get(moduleName);
  }

  getModuleDescriptors() {
    return this._moduleDescriptors.toArray();
  }

  /**
   * Returns ids of all enabled modules
   * TODO: use static config.json - some modules can be used as internal dependency
   */
  getEnabledModuleIds() {
    return this.getModuleDescriptors().map(moduleDescriptor => {
      return moduleDescriptor.id;
    });
  }

}
