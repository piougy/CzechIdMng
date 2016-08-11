

import { formatPattern } from 'react-router/lib/PatternUtils';
import Immutable from 'immutable';
// reused actions
import FlashMessagesManager from '../../modules/core/redux/flash/FlashMessagesManager';
// api
import { ConfigService, LocalizationService } from '../../modules/core/services';
import SecurityManager from '../../modules/core/redux/security/SecurityManager';
/*
 * action types
 */
export const SELECT_NAVIGATION_ITEMS = 'SELECT_NAVIGATION_ITEMS';
export const SELECT_NAVIGATION_ITEM = 'SELECT_NAVIGATION_ITEM';
export const I18N_INIT = 'I18N_INIT';
export const I18N_READY = 'I18N_READY';
export const NAVIGATION_INIT = 'NAVIGATION_INIT';

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
* After localization is initied - set to ready, before inicialization will be false
*/
export function i18nReady(ready) {
  return {
    type: I18N_READY,
    ready
  };
}

/**
* Init i18n localization service
*/
export function i18nInit() {
  return (dispatch) => {
    const localizationService = new LocalizationService(
      (error) => {
        if (error) {
          const flashMessagesManager = new FlashMessagesManager();
          dispatch(flashMessagesManager.addMessage({level: 'error', title: 'Nepodařilo se iniciovat lokalizaci', message: error }));
        }
        dispatch(i18nReady(true, localizationService));
      }
    );
  };
}

/**
 * Reloads whole navigation
 * @deprecated - navigation is loaded synchronously to layout reducer
 */
export function navigationInit() {
  const configService = new ConfigService();
  return {
    type: NAVIGATION_INIT,
    navigation: configService.getNavigation()
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
    navigationItems = navigation.get(ConfigService.NAVIGATION_BY_PARENT).get('').toArray();
  } else {
    if (!navigation.get(ConfigService.NAVIGATION_BY_PARENT).has(parentId)) {
      return [];
    }
    navigationItems = navigation.get(ConfigService.NAVIGATION_BY_PARENT).get(parentId).toArray();
  }

  return navigationItems.filter(item => {
    if (section && section !== item.section) {
      return false;
    }
    if (item.disabled) {
      return false;
    }
    // security check
    if (!SecurityManager.hasAccess(userContext, item.access)) {
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
  if (!navigation.get(ConfigService.NAVIGATION_BY_ID).has(id)) {
    return null;
  }
  return navigation.get(ConfigService.NAVIGATION_BY_ID).get(id);
}
