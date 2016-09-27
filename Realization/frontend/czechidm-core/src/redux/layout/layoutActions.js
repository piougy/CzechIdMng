import { formatPattern } from 'react-router/lib/PatternUtils';
import Immutable from 'immutable';
// reused actions
import FlashMessagesManager from '../../redux/flash/FlashMessagesManager';
// api
import ConfigLoader from '../../utils/ConfigLoader';
import ComponentLoader from '../../utils/ComponentLoader';
import ModuleLoader from '../../utils/ModuleLoader';
import { LocalizationService } from '../../services';
import SecurityManager from '../security/SecurityManager';
import ConfigurationManager from '../data/ConfigurationManager';
/*
 * action types
 */
export const SELECT_NAVIGATION_ITEMS = 'SELECT_NAVIGATION_ITEMS';
export const SELECT_NAVIGATION_ITEM = 'SELECT_NAVIGATION_ITEM';
export const I18N_INIT = 'I18N_INIT';
export const I18N_READY = 'I18N_READY';
export const APP_INIT = 'APP_INIT';
export const APP_READY = 'APP_READY';
export const APP_UNAVAILABLE = 'APP_UNAVAILABLE';
export const NAVIGATION_INIT = 'NAVIGATION_INIT';
export const NAVIGATION_READY = 'NAVIGATION_READY';
export const MODULES_INIT = 'MODULES_INIT';
export const MODULES_READY = 'MODULES_READY';
export const CONFIGURATION_INIT = 'CONFIGURATION_INIT';
export const CONFIGURATION_READY = 'CONFIGURATION_READY';

/**
 * Select navigation items
 *
 * @param  {array} selectedNavigationItems Array of selected navigation item. Can contains null values for select specified navigation level
 */
export function selectNavigationItems(selectedNavigationItems) {
  return {
    type: SELECT_NAVIGATION_ITEMS,
    selectedNavigationItems
  };
}
/**
 * Select navigation item bz id
 * @return {[type]} [description]
 */
export function selectNavigationItem(selectedNavigationItemId) {
  return {
    type: SELECT_NAVIGATION_ITEM,
    selectedNavigationItemId
  };
}

/**
 * Reloads whole navigation
 */
function navigationInit() {
  return (dispatch) => {
    dispatch({
      type: NAVIGATION_INIT
    });
    const configLoader = new ConfigLoader();
    dispatch({
      type: NAVIGATION_READY,
      navigation: configLoader.getNavigation(),
      ready: true
    });
    dispatch({
      type: APP_READY,
      ready: true
    });
  };
}

export function backendConfigurationInit() {
  return (dispatch, getState) => {
    const configurationManager = new ConfigurationManager();
    dispatch(configurationManager.fetchPublicConfigurations((data, error) => {
      if (!error) {
        dispatch({
          type: CONFIGURATION_READY,
          ready: true
        });
        // disable modules by configuration
        ModuleLoader.getModuleDescriptors().forEach(moduleDescriptor => {
          if (moduleDescriptor.backendId) { // FE module depends on be module
            const isEnabled = ConfigurationManager.isModuleEnabled(getState(), moduleDescriptor.backendId) || false;
            ModuleLoader.enable(moduleDescriptor.id, isEnabled);
          } else {
            const isEnabled = ConfigurationManager.isModuleEnabled(getState(), moduleDescriptor.id);
            ModuleLoader.enable(moduleDescriptor.id, isEnabled === null || isEnabled);
          }
        });
        dispatch(navigationInit());
      } else {
        const flashMessagesManager = new FlashMessagesManager();
        dispatch(flashMessagesManager.addUnavailableMessage());
        dispatch({
          type: APP_UNAVAILABLE
        });
      }
    }));
  };
}

/**
* After localization is initied - set to ready, before inicialization will be false
*/
function i18nReady(ready) {
  return (dispatch) => {
    dispatch({
      type: I18N_READY,
      ready
    });
    dispatch(backendConfigurationInit());
  };
}

/**
* Init i18n localization service
*/
function i18nInit() {
  return (dispatch) => {
    LocalizationService.init(new ConfigLoader(),
      (error) => {
        if (error) {
          const flashMessagesManager = new FlashMessagesManager();
          dispatch(flashMessagesManager.addMessage({level: 'error', title: 'Nepodařilo se iniciovat lokalizaci', message: error }));
        } else {
          dispatch(i18nReady(true));
        }
      }
    );
  };
}

function frontendConfigurationInit(config) {
  return (dispatch) => {
    dispatch({
      type: CONFIGURATION_INIT
    });
    // FE configuration
    ConfigLoader.initConfig(config);
    dispatch(i18nInit());
  };
}

/**
* Init modules
*/
function modulesInit(config, moduleDescriptores, componentDescriptors) {
  return (dispatch) => {
    dispatch({
      type: MODULES_INIT
    });
    ModuleLoader.init(moduleDescriptores);
    ComponentLoader.initComponents(componentDescriptors);
    dispatch({
      type: MODULES_READY,
      ready: true
    });
    dispatch(frontendConfigurationInit(config));
  };
}

export function appInit(config, moduleDescriptors, componentDescriptors) {
  return (dispatch) => {
    // init application - start asynchronous inicialization: modules / localization / configuration / navigation
    dispatch(modulesInit(config, moduleDescriptors, componentDescriptors));
  };
}

/**
 * return parameters used in redirections
 */
export function resolveNavigationParameters(userContext = null, params = null) {
  let parameterValues;
  if (params) {
    parameterValues = new Immutable.Map(params);
  } else {
    parameterValues = new Immutable.Map({});
  }
  if (userContext) {
    //
    parameterValues = parameterValues.set('loggedUsername', userContext.username || 'guest');
    if (!parameterValues.has('userID')) {
      parameterValues = parameterValues.set('userID', userContext.username || 'guest');
    }
  }
  return parameterValues.toJS();
}

/**
 * Return navigation items for given level (parentId).
 * If parentId isn't set, then returns root (top) navigation items
 * Check user access, conditions etc ...
 * construct target links by given parameters
 */
export function getNavigationItems(navigation, parentId = null, section = null, userContext = null, params = null) {
  if (!navigation) {
    return [];
  }
  let navigationItems = [];
  if (!parentId) {
    navigationItems = navigation.get(ConfigLoader.NAVIGATION_BY_PARENT).get('').toArray();
  } else {
    if (!navigation.get(ConfigLoader.NAVIGATION_BY_PARENT).has(parentId)) {
      return [];
    }
    navigationItems = navigation.get(ConfigLoader.NAVIGATION_BY_PARENT).get(parentId).toArray();
  }

  return navigationItems.filter(item => {
    if (section && section !== item.section) {
      return false;
    }
    if (item.disabled) {
      return false;
    }
    // security check
    if (!SecurityManager.hasAccess(item.access, userContext)) {
      return false;
    }
    // construct target to link from path and parameters
    if (item.path) {
      item.to = formatPattern(item.path, resolveNavigationParameters(userContext, params));
    }

    return true;
  });
}

/**
 * Returns navigation itm by given id
 *
 * @param  {immutable.map} navigation
 * @param  {string} id
 * @return {navigationItem}
 */
export function getNavigationItem(navigation, id) {
  if (!navigation || !id) {
    return null;
  }
  if (!navigation.get(ConfigLoader.NAVIGATION_BY_ID).has(id)) {
    return null;
  }
  return navigation.get(ConfigLoader.NAVIGATION_BY_ID).get(id);
}
