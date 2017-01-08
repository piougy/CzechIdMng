import { Enums } from 'czechidm-core';

/**
 * OperationType for adit operation etc.
 */
export default class ProvisioningOperationTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.ProvisioningOperationTypeEnum.${key}`);
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
      case this.CREATE: {
        return 'success';
      }
      case this.UPDATE: {
        return 'info';
      }
      default: {
        return 'danger';
      }
    }
  }
}

ProvisioningOperationTypeEnum.CREATE = Symbol('CREATE');
ProvisioningOperationTypeEnum.UPDATE = Symbol('UPDATE');
ProvisioningOperationTypeEnum.DELETE = Symbol('DELETE');
