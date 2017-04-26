import AbstractEnum from '../enums/AbstractEnum';

/**
 * Script authority type enum
 */
export default class ScriptAuthorityTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.ScriptAuthorityTypeEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }

  static getLevel(key) {
    if (!key) {
      return null;
    }
    //
    const sym = super.findSymbolByKey(this, key);
    //
    switch (sym) {
      case this.SERVICE: {
        return 'info';
      }
      case this.CLASS_NAME: {
        return 'success';
      }
      default: {
        return 'default';
      }
    }
  }
}

ScriptAuthorityTypeEnum.SERVICE = Symbol('SERVICE');
ScriptAuthorityTypeEnum.CLASS_NAME = Symbol('CLASS_NAME');
