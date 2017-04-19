import EntityUtils from './EntityUtils';

/**
 * Helper methods for ui state
 *
 * @author Radek Tomiška
 */
export default class UiUtils {

  /**
   * Returns state associated with given key or null if state for given key does not exist
   *
   * @param  {state} state - application state
   * @param  {string} uiKey - ui key for loading indicator etc.
   * @return {object} - ui state
   */
  static getUiState(state, uiKey) {
    if (!state || !uiKey || !state.data.ui[uiKey]) {
      return null;
    }
    return state.data.ui[uiKey];
  }

  /**
   * Returns true, when loading for given uiKey proceed
   *
   * @param  {state} state - application state
   * @param  {string} uiKey - ui key for loading indicator etc.
   * @return {boolean} - true, when loading for given uiKey proceed
   */
  static isShowLoading(state, uiKey) {
    const uiState = UiUtils.getUiState(state, uiKey);
    if (!uiState) {
      return false;
    }
    return uiState.showLoading;
  }

  /**
   * Returns error asigned to given uiKey or null, if no error is found
   *
   * @param  {state} state - application state
   * @param  {string} uiKey - ui key for loading indicator etc.
   * @return {object} error
   */
  static getError(state, uiKey) {
    const uiState = UiUtils.getUiState(state, uiKey);
    if (!uiState) {
      return null;
    }
    if (!uiState.error) {
      return null;
    }
    return uiState.error;
  }

  /**
   * Returns search parameters associated with given ui key. If state for given key does not exist, then returns defaultSearchParameters
   *
   * @param  {state} state - application state
   * @param  {string} uiKey - ui key for loading indicator etc.
   * @return {boolean} - true, when loading for given uiKey proceed
   */
  static getSearchParameters(state, uiKey, defaultSearchParameters = {}) {
    const uiState = UiUtils.getUiState(state, uiKey);
    if (!uiState) {
      return false;
    }
    return uiState.searchParameters ? uiState.searchParameters : defaultSearchParameters;
  }

  /**
   * Read entities associarted by given uiKey items from ui store
   *
   * @param  {state} state [description]
   * @param  {string} uiKey - ui key for loading indicator etc.
   * @return {array[entity]}
   */
  static getEntities(state, uiKey) {
    const uiState = UiUtils.getUiState(state, uiKey);
    if (!uiState) {
      return [];
    }
    return EntityUtils.getEntitiesByIds(state, uiState.entityType, uiState.items, uiState.trimmed);
  }

  /**
   * Returns css row class for given entity
   * - when entity is disabled - returns `disabled`
   * - when entity is invalid - returns `disabled`
   * - otherwise: empty string
   *
   * @param  {object} entity
   * @return {string} css row class
   */
  static getRowClass(entity) {
    if (!entity) {
      return '';
    }
    if (EntityUtils.isDisabled(entity)) {
      return 'disabled';
    }
    if (!EntityUtils.isValid(entity)) {
      return 'disabled';
    }
    return '';
  }

  /**
   * Returns css row class for given entity
   * - when entity is disabled - returns `disabled`
   * - otherwise: empty string
   *
   * @param  {object} entity
   * @return {string} css row class
   */
  static getDisabledRowClass(entity) {
    if (!entity) {
      return '';
    }
    if (EntityUtils.isDisabled(entity)) {
      return 'disabled';
    }
    return '';
  }

  /**
   * Returns random css level
   *
   * @return {string}
   */
  static getRandomLevel() {
    const min = Math.ceil(0);
    const max = Math.floor(6);
    const levelNumber = Math.floor(Math.random() * (max - min)) + min;

    switch (levelNumber) {
      case 5: {
        return 'danger';
      }
      case 4: {
        return 'warning';
      }
      case 3: {
        return 'info';
      }
      case 2: {
        return 'success';
      }
      default: {
        return 'primary';
      }
    }
  }


  /**
   * Return simple class name
   *
   * @param  {string} taskType cannonical class name
   * @return {string}
   */
  static getSimpleJavaType(javaType) {
    if (!javaType) {
      return null;
    }
    return javaType.split('.').pop(-1);
  }

/**
 * Do substring on given data by max length. Substring is not on char byt on word.
 * Last word will be whole.
 * @param  {[type]} data      [description]
 * @param  {[type]} maxLength [description]
 * @return {[type]}           [description]
 */
  static substringByWord(data, maxLength) {
    if (data) {
      data = data + ' ';
      const result = data.replace(/<(?:.|\n)*?>/gm, '').substr(0, maxLength);
      return result.substr(0, Math.min(result.length, result.lastIndexOf(' ')));
    }
    return null;
  }
}
