'use strict';

/**
 * action types
 */
export const CACHE_ITEMS = 'CACHE_ITEMS';
export const EDIT_ITEM = 'EDIT_ITEM';
export const CANCEL_EDIT_ITEM = 'CANCEL_EDIT_ITEM';
export const APPLY_EDIT_ITEM = 'APPLY_EDIT_ITEM';
export const CANCEL_FORM = 'CANCEL_FORM';
export const ADD_ITEM = 'ADD_ITEM';
export const REMOVE_ITEM = 'REMOVE_ITEM';
export const STORE_DATA = 'STORE_DATA';
export const CLEAR_DATA = 'CLEAR_DATA';

/**
 * Encapsulate redux action for form data (create, edit) etc.
 */
export default class FormManager {

  cacheItems(key, items) {
    return {
      type: CACHE_ITEMS,
      items: items,
      key: key
    };
  }

  editItem(key, id){
    return {
      type: EDIT_ITEM,
      id: id,
      key: key
    };
  }

  cancelEditItem(key, id){
    return {
      type: CANCEL_EDIT_ITEM,
      id: id,
      key: key
    };
  }

  applyEditItem(key, id, data){
    return {
      type: APPLY_EDIT_ITEM,
      id: id,
      key: key,
      data: data
    };
  }

  cancelForm(key){
    return {
      type: CANCEL_FORM,
      key: key
    };
  }

  addItem(key, uuid){
    return {
      type: ADD_ITEM,
      key: key,
      uuid: uuid
    };
  }

  removeItem(key, id){
    return {
      type: REMOVE_ITEM,
      key: key,
      id: id
    };
  }

  /**
   * Add data to store - data can be read by other components
   *
   * @param  {string} uiKey - access ui key
   * @param  {any} data - stored data
   * @return {action} - action
   */
  storeData(uiKey, data) {
    return {
      type: STORE_DATA,
      uiKey: uiKey,
      data: data
    };
  }

  /**
   * Clear (remove) data from store
   *
   * @param  {string} uiKey - access ui key
   * @return {action} - action
   */
  clearData(uiKey) {
    return {
      type: CLEAR_DATA,
      uiKey: uiKey
    };
  }

  /**
   *
   *
   * @param  {state} state - application state
   * @param  {string} uiKey - access ui key
   * @return {any} - stored data
   */
  static getData(state, uiKey) {
    if (!state || !uiKey) {
      return null;
    }
    if (!state.data.data.has(uiKey)) {
      return null;
    }
    return state.data.data.get(uiKey);
  }
}
