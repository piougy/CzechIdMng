/**
 * action types
 */
export const REQUEST_DATA = 'REQUEST_DATA';
export const RECEIVE_DATA = 'RECEIVE_DATA';
export const CLEAR_DATA = 'CLEAR_DATA';

/**
 * Encapsulate redux action for form data (create, edit) etc.
 */
export default class DataManager {

  /**
   * Request data from store - simply sets loading flag fot given uiKey
   *
   * @param  {string} uiKey - access ui key
   * @param  {any} data - stored data
   * @return {action} - action
   */
  requestData(uiKey) {
    return {
      type: REQUEST_DATA,
      uiKey
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
    return this.receiveData(uiKey, data);
  }

  /**
   * Add data to store - data can be read by other components
   *
   * @param  {string} uiKey - access ui key
   * @param  {any} data - stored data
   * @return {action} - action
   */
  receiveData(uiKey, data) {
    return {
      type: RECEIVE_DATA,
      uiKey,
      data
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
      uiKey
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
