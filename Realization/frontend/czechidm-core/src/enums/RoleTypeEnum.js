

import AbstractEnum from '../enums/AbstractEnum';

/**
 * OperationType for adit operation etc.
 */
export default class RoleTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.RoleTypeEnum.${key}`);
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
      case this.BUSINESS: {
        return 'info';
      }
      case this.TECHNICAL: {
        return 'success';
      }
      default: {
        return 'default';
      }
    }
  }
}

RoleTypeEnum.SYSTEM = Symbol('SYSTEM');
RoleTypeEnum.BUSINESS = Symbol('BUSINESS');
RoleTypeEnum.TECHNICAL = Symbol('TECHNICAL');
RoleTypeEnum.LOGIN = Symbol('LOGIN');
