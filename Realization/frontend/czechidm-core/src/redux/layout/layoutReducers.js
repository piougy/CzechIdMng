import Immutable from 'immutable';
//
import {
  SELECT_NAVIGATION_ITEMS,
  SELECT_NAVIGATION_ITEM,
  COLLAPSE_NAVIGATION,
  I18N_INIT,
  I18N_READY,
  MODULES_INIT,
  MODULES_READY,
  NAVIGATION_INIT,
  NAVIGATION_READY,
  CONFIGURATION_INIT,
  CONFIGURATION_READY,
  APP_INIT,
  APP_READY,
  APP_UNAVAILABLE,
  getNavigationItem
} from './layoutActions';

const INITIAL_STATE = new Immutable.Map({
  navigation: null,  // all navigation items from enabled modules as Map
  selectedNavigationItems: ['home'], // homepage by default
  navigationCollapsed: false, // TODO: move to local storage - different reducer
  i18nReady: false,              // localization context is ready
  modulesReady: false,            // modules loaders is ready
  navigationReady: false,
  configurationReady: false,
  appReady: false,
  appUnavailable: false
});

export function layout(state = INITIAL_STATE, action) {
  switch (action.type) {
    case SELECT_NAVIGATION_ITEMS: {
      const prevState = state.get('selectedNavigationItems');
      const newState = [];
      for (let i = 0; i < action.selectedNavigationItems.length; i++) {
        newState[i] = action.selectedNavigationItems[i] || (prevState.length > i ? prevState[i] : null);
      }
      return state.set('selectedNavigationItems', newState);
    }
    case SELECT_NAVIGATION_ITEM: {
      const newState = [];
      // traverse to item parent
      let itemId = action.selectedNavigationItemId;
      while (itemId !== null) {
        const item = getNavigationItem(state, itemId);
        if (!item) {
          break;
        }
        newState.splice(0, 0, item.id); // insert at start
        itemId = item.parentId;
      }
      return state.set('selectedNavigationItems', newState);
    }
    case COLLAPSE_NAVIGATION: {
      return state.set('navigationCollapsed', action.collapsed);
    }
    case I18N_INIT: {
      return state.set('i18nReady', false);
    }
    case I18N_READY: {
      LOGGER.debug('i18n ready [' + action.ready + ']');
      return state.set('i18nReady', action.ready);
    }
    case MODULES_INIT: {
      return state.set('modulesReady', false);
    }
    case MODULES_READY: {
      LOGGER.debug('modules ready [' + action.ready + ']');
      return state.set('modulesReady', action.ready);
    }
    case NAVIGATION_INIT: {
      return state.set('navigationReady', false);
    }
    case NAVIGATION_READY: {
      LOGGER.debug('navigation ready [' + action.ready + ']');
      let newState = state.set('navigation', action.navigation);
      newState = newState.set('navigationReady', action.ready);
      return newState;
    }
    case CONFIGURATION_INIT: {
      return state.set('configurationReady', false);
    }
    case CONFIGURATION_READY: {
      LOGGER.debug('configuration ready [' + action.ready + ']');
      return state.set('configurationReady', action.ready);
    }
    case APP_INIT: {
      let newState = state.set('appReady', false);
      newState = newState.set('appUnavailable', false);
      return newState;
    }
    case APP_READY: {
      LOGGER.debug('app ready [' + action.ready + ']');
      return state.set('appReady', action.ready);
    }
    case APP_UNAVAILABLE: {
      let newState = state.set('appReady', false);
      newState = newState.set('appUnavailable', true);
      return newState;
    }
    default:
      return state;
  }
}
