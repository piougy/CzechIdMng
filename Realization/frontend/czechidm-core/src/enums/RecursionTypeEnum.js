import AbstractEnum from '../enums/AbstractEnum';

/**
 * Recursion type - used for automatic role etc.
 *
 * @author Radek Tomiška
 */
export default class RecursionTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.RecursionTypeEnum.${key}`);
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

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.NO: {
        return 'success';
      }
      default: {
        return 'info';
      }
    }
  }
}

RecursionTypeEnum.NO = Symbol('NO');
RecursionTypeEnum.DOWN = Symbol('DOWN');
// not implemented for now
// RecursionTypeEnum.UP = Symbol('UP');
