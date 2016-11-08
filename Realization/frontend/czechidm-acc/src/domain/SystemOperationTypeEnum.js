import { Enums } from 'czechidm-core';

/**
 * SystemOperationType for provisioning and maping.
 */
export default class SystemOperationTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.SystemOperationTypeEnum.${key}`);
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
      case this.PROVISIONING: {
        return 'success';
      }
      default: {
        return 'default';
      }
    }
  }
}

SystemOperationTypeEnum.PROVISIONING = Symbol('PROVISIONING');
SystemOperationTypeEnum.RECONCILIACE = Symbol('RECONCILIACE');
SystemOperationTypeEnum.SYNCHRONISATION = Symbol('SYNCHRONISATION');
