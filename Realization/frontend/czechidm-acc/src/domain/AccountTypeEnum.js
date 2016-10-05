import { Enums } from 'czechidm-core';

/**
 * OperationType for adit operation etc.
 */
export default class AccountTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.AccountTypeEnum.${key}`);
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
      case this.PERSONAL: {
        return 'success';
      }
      case this.TECHNICAL: {
        return 'primary';
      }
      default: {
        return 'default';
      }
    }
  }
}

AccountTypeEnum.PERSONAL = Symbol('PERSONAL');
AccountTypeEnum.TECHNICAL = Symbol('TECHNICAL');
