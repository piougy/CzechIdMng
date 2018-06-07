import { Enums } from 'czechidm-core';
import TreeAttributeEnum from './TreeAttributeEnum';
import RoleAttributeEnum from './RoleAttributeEnum';
import RoleCatalogueAttributeEnum from './RoleCatalogueAttributeEnum';

/**
 * System entity type
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
      case this.ROLE_CATALOGUE: {
        return 'primary';
      }
      case this.CONTRACT: {
        return 'success';
      }
      case this.CONTRACT_SLICE: {
        return 'success';
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
        return Enums.IdentityAttributeEnum;
      }
      case this.TREE: {
        return TreeAttributeEnum;
      }
      case this.ROLE: {
        return RoleAttributeEnum;
      }
      case this.ROLE_CATALOGUE: {
        return RoleCatalogueAttributeEnum;
      }
      case this.CONTRACT: {
        return Enums.ContractAttributeEnum;
      }
      case this.CONTRACT_SLICE: {
        return Enums.ContractSliceAttributeEnum;
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
SystemEntityTypeEnum.ROLE_CATALOGUE = Symbol('ROLE_CATALOGUE');
SystemEntityTypeEnum.CONTRACT = Symbol('CONTRACT');
SystemEntityTypeEnum.CONTRACT_SLICE = Symbol('CONTRACT_SLICE');
