

import AbstractEnum from '../enums/AbstractEnum';

/**
 * AutomaticRoleRequestType enumeration for automatic roles
 *
 * @author Vít Švanda
 */
export default class AutomaticRoleRequestTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.AutomaticRoleRequestTypeEnum.${key}`);
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
      case this.TREE: {
        return 'info';
      }
      case this.ATTRIBUTE: {
        return 'success';
      }
      default: {
        return 'default';
      }
    }
  }
}

AutomaticRoleRequestTypeEnum.TREE = Symbol('TREE');
AutomaticRoleRequestTypeEnum.ATTRIBUTE = Symbol('ATTRIBUTE');
