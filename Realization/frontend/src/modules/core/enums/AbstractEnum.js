

import { i18n } from '../services/LocalizationService';

/**
 * workaround for enumeration in javascript
 */
export default class AbstractEnum {

  /**
   * Returns localized message
   * TODO: access to LocalizationService in this layer is maybe inappropriate - move localization outside of this class
   */
  static getNiceLabel(key) {
    return i18n(key);
  }

  /**
   * Returns level decorator (Label)
   */
  static getLevel() {
    return null;
  }

  /**
   * finds key to given symbol
   */
  static findKeyBySymbol(enumeration, sym){
    if (sym){
      for (let enumItem in enumeration) {
        if (enumeration[enumItem] === sym){
            return enumItem;
        }
      }
    }
  }

  /**
   * find symbol by key
   */
  static findSymbolByKey(enumeration, key){
    if (key){

      for (let enumItem in enumeration) {
        if (enumItem === key){
            return enumeration[enumItem];
        }
      }
    }
  }
}
