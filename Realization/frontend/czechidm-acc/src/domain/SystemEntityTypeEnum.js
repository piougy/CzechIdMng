import { Enums } from 'czechidm-core';
import IdentityAttributeEnum from './IdentityAttributeEnum';
import TreeAttributeEnum from './TreeAttributeEnum';
import RoleAttributeEnum from './RoleAttributeEnum';

/**
 * OperationType for adit operation etc.
 */
export default class SystemEntityTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.SystemEntityTypeEnum.${key}`);
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
      case this.IDENTITY: {
        return 'success';
      }
      case this.TREE: {
        return 'primary';
      }
      case this.ROLE: {
        return 'primary';
      }
      default: {
        return 'default';
      }
    }
  }
  static getEntityEnum(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.IDENTITY: {
        return IdentityAttributeEnum;
      }
      case this.TREE: {
        return TreeAttributeEnum;
      }
      case this.ROLE: {
        return RoleAttributeEnum;
      }
      default: {
        return null;
      }
    }
  }
}

SystemEntityTypeEnum.IDENTITY = Symbol('IDENTITY');
SystemEntityTypeEnum.TREE = Symbol('TREE');
SystemEntityTypeEnum.ROLE = Symbol('ROLE');
