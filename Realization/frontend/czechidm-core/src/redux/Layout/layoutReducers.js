import Immutable from 'immutable';
//
import { SELECT_NAVIGATION_ITEMS, SELECT_NAVIGATION_ITEM, I18N_READY, MODULES_LOADER_READY, NAVIGATION_INIT, getNavigationItem } from './layoutActions';

const INITIAL_STATE = new Immutable.Map({
  navigation: null, // configLoader.getNavigation(), // all navigation items from enabled modules as Map
  selectedNavigationItems: ['home'], // homepage by default
  i18nReady: false,              // localization context is ready
  modulesReady: false            // modules loaders is ready
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
        const item = getNavigationItem(state.get('navigation'), itemId);
        if (!item) {
          break;
        }
        newState.splice(0, 0, item.id); // insert at start
        itemId = item.parentId;
      }
      return state.set('selectedNavigationItems', newState);
    }
    case I18N_READY: {
      return state.set('i18nReady', action.ready);
    }
    case MODULES_LOADER_READY: {
      return state.set('modulesReady', action.ready);
    }
    case NAVIGATION_INIT: {
      return state.set('navigation', action.navigation);
    }
    default:
      return state;
  }
}
