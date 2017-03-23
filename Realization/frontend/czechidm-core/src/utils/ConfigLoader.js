import Immutable from 'immutable';
import _ from 'lodash';
//
let _config = null;
let _moduleDescriptors = new Immutable.Map();

// suported and default values for navigation item in module descriptor
const MODULE_DESCRIPTOR_DEFAULTS = {
  'id': undefined,
  'type': 'DYNAMIC',
  'section': 'main',
  'label': undefined,
  'labelKey': undefined,
  'title': undefined,
  'titleKey': undefined,
  'icon': undefined,
  'order': 0,
  'priority': 0,
  'path': undefined,
  'access': [
    {
      'type': 'IS_AUTHENTICATED'
    }
  ],
  'items': []
};

/**
* Loads frontend configuration
*
* @author Radek Tomiška
*/
export default class ConfigLoader {

  static init(jsonConfig, moduleDescriptors) {
    _config = jsonConfig;
    _moduleDescriptors = moduleDescriptors;
  }

  /**
   * Returns config part by key or null
   */
  static getConfig(key) {
    return _config[key];
  }

  /**
   * Returns BE server base url
   * @return {string}
   */
  static getServerUrl() {
    return serverUrl;
  }

  static _getModuleDescriptor(moduleId) {
    if (!_moduleDescriptors.has(moduleId)) {
      return null;
    }
    return _moduleDescriptors.get(moduleId);
  }

  /**
   * Returns Module descriptor for given module id
   *
   * @param  {string} moduleId
   * @return {ModuleDescriptor} json object
   */
  static getModuleDescriptor(moduleId) {
    const loaderModuleDescriptor = this._getModuleDescriptor(moduleId);
    const configModuleDescriptor = this._getConfigModuleDescriptor(moduleId);
    // Merge module descriptor with override values from configuration
    return _.mergeWith(loaderModuleDescriptor, configModuleDescriptor, this._overrideModuleDescriptorMerge.bind(this));
  }

  static getModuleDescriptors() {
    const moduleDescriptors = [];
    _moduleDescriptors.forEach(moduleDescriptor => {
      moduleDescriptors.push(this.getModuleDescriptor(moduleDescriptor.id));
    });
    return moduleDescriptors;
  }

  /**
   * Returns enabled module ids
   *
   * @return {array[string]}
   */
  static getEnabledModuleIds() {
    const enabledModuleIds = [];
    this.getModuleDescriptors().forEach(moduleDescriptor => {
      if (this.isEnabled(moduleDescriptor)) {
        enabledModuleIds.push(moduleDescriptor.id);
      }
    });
    return enabledModuleIds;
  }

  /**
   * Return true, if frontend module is enabled, otherwise false (not enabled or module is not installed).
   *
   * @param  {moduleDescriptor} moduleDescriptor
   * @return {Boolean}
   */
  static isEnabled(moduleDescriptor) {
    if (moduleDescriptor === null) {
      return false;
    }
    return moduleDescriptor.enabled === undefined || moduleDescriptor.enabled === null || moduleDescriptor.enabled !== false;
  }

  /**
   * Return true, if frontend mudel is enabled, otherwise false (not enabled or module is not installed).
   *
   * @param  {string} moduleId
   * @return {Boolean}
   */
  static isEnabledModule(moduleId) {
    // enabled setting could be overriden by frontend configuration
    const moduleDescriptor = this.getModuleDescriptor(moduleId);
    return this.isEnabled(moduleDescriptor);
  }

  static enable(moduleId, enabled = true) {
    const moduleDescriptor = this._getModuleDescriptor(moduleId);
    _moduleDescriptors = _moduleDescriptors.set(moduleId, _.merge({}, moduleDescriptor, { enabled }));
  }


  static _getConfigModuleDescriptor(moduleId) {
    if (_config && _config.overrideModuleDescriptor) {
      return _config.overrideModuleDescriptor[moduleId];
    }
    return {};
  }

  /**
   * Function for lodash mergeWith. Is use for custom merge override module descriptors from configuration.
   */
  static _overrideModuleDescriptorMerge(objValue, srcValue) {
    let standardMerge = false;
    if (_.isArray(objValue)) {
      for (const value of objValue) {
        for (const overrideValue of srcValue) {
          if (overrideValue && value && overrideValue.id && value.id && overrideValue.id === value.id) {
            _.mergeWith(value, overrideValue, this._overrideModuleDescriptorMerge.bind(this));
          }
          // Item not have id ... we will use standard merge for this array
          if (value && !value.id) {
            standardMerge = true;
          }
        }
      }
      if (!standardMerge) {
        // we did merge in this array itself. Return array as resutl.
        return objValue;
      }
      // standard merge
    }
  }


  /**
   * Append module navigation to items
   * - works with order, priority etc
   */
  static _resolveNavigation(navigationItems, moduleId) {
    const moduleDescriptor = this.getModuleDescriptor(moduleId);

    if (!moduleDescriptor.navigation) {
      return this._navigationItems;
    }
    // items
    navigationItems = this._appendNavigationItems(navigationItems, '', moduleDescriptor.navigation.items);
    // routes access
    navigationItems = this._appendNavigationAccess(navigationItems, moduleDescriptor.navigation.access);
    //
    return navigationItems;
  }

  static _appendNavigationItems(navigationItems, parentId, rawItems) {
    if (!rawItems) {
      return navigationItems;
    }
    //
    for (let i = 0; i < rawItems.length; i++) {
      // append default values
      const item = _.merge({ parentId }, MODULE_DESCRIPTOR_DEFAULTS, rawItems[i]);
      const _parentId = item.parentId || parentId;
      //
      let items = (navigationItems.get(this.NAVIGATION_BY_PARENT).has(_parentId)) ? navigationItems.get(this.NAVIGATION_BY_PARENT).get(_parentId) : new Immutable.Map();
      // first or higher priority wins
      if (!items.has(item.id) || items.get(item.id).priority < item.priority) {
        items = items.set(item.id, item);
        navigationItems = navigationItems.set(this.NAVIGATION_BY_ID, navigationItems.get(this.NAVIGATION_BY_ID).set(item.id, item));
      }
      navigationItems = navigationItems.set(this.NAVIGATION_BY_PARENT, navigationItems.get(this.NAVIGATION_BY_PARENT).set(_parentId, items));
      navigationItems = this._appendNavigationItems(navigationItems, item.id, item.items);
    }
    return navigationItems;
  }

  static _appendNavigationAccess(navigationItems, rawAccess) {
    if (!rawAccess) {
      return navigationItems;
    }
    rawAccess.forEach(item => {
      let items = (navigationItems.get(this.NAVIGATION_BY_PATH).has(item.path)) ? navigationItems.get(this.NAVIGATION_BY_PATH).get(item.path) : new Immutable.List();
      items = items.push(item);
      navigationItems = navigationItems.set(this.NAVIGATION_BY_PATH, navigationItems.get(this.NAVIGATION_BY_PATH).set(item.path, items));
    });
    return navigationItems;
  }

  /**
   * Loads navigation items from module descriptors
   *
   * @return Immutable.Map({ byParent: Immutable.Map, byId: Immutable.Map. byPath: })
   */
  static getNavigation() {
    let navigationItems = new Immutable.Map({
      [this.NAVIGATION_BY_PARENT]: new Immutable.Map({}),
      [this.NAVIGATION_BY_ID]: new Immutable.Map({}),
      [this.NAVIGATION_BY_PATH]: new Immutable.Map({})
    });
    this.getEnabledModuleIds().map(moduleId => {
      navigationItems = this._resolveNavigation(navigationItems, moduleId);
    });
    // order
    navigationItems = navigationItems.set(
      this.NAVIGATION_BY_PARENT,
      navigationItems.get(this.NAVIGATION_BY_PARENT)
        .mapEntries(([k, v]) => [k, v.sortBy(item => item.order)])
    );
    //
    return navigationItems;
  }

  /**
   * Returns navigation items for given parent. If not parentId is suplied, then returns root navigation items
   * @return array of items
   */
  getNavigationItems(parentId = null) {
    // load all module descriptor
    const navigationItems = this.getNavigation().get(this.NAVIGATION_BY_PARENT);
    // level by parentId
    if (!parentId) {
      return navigationItems.get('').toArray();
    }
    if (!navigationItems.has(parentId)) {
      return [];
    }
    return navigationItems.get(parentId).toArray();
  }
}

ConfigLoader.NAVIGATION_BY_PARENT = 'byParent';
ConfigLoader.NAVIGATION_BY_ID = 'byId';
ConfigLoader.NAVIGATION_BY_PATH = 'byPath';
