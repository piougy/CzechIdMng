import AbstractEnum from '../enums/AbstractEnum';

/**
 * Identity contract state
 *
 * @author Radek Tomiška
 */
export default class ContractStateEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.ContractStateEnum.${key}`);
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
    switch (sym) {
      case this.EXCLUDED: {
        return 'warning';
      }
      case this.DISABLED: {
        return 'danger';
      }
      default: {
        return 'default';
      }
    }
  }

  /**
   * State leeds to disabled contract
   *
   * @param  {string} key
   * @return {Boolean}
   */
  static isContractDisabled(key) {
    const sym = super.findSymbolByKey(this, key);
    //
    switch (sym) {
      case this.EXCLUDED: {
        return false;
      }
      default: {
        return true;
      }
    }
  }
}

ContractStateEnum.EXCLUDED = Symbol('EXCLUDED');
ContractStateEnum.DISABLED = Symbol('DISABLED');
