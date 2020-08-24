import AbstractEnum from './AbstractEnum';

/**
 * OperationType for adit operation etc.
 *
 * @author Radek Tomiška
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
      case this.SYSTEM: {
        return 'info';
      }
      default: {
        return 'default';
      }
    }
  }
}

RoleTypeEnum.SYSTEM = Symbol('SYSTEM');
