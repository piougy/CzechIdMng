import { Enums } from 'czechidm-core';

/**
 * Empty provisioning type - used in filter.
 *
 * @author Radek Tomiška
 * @since 10.6.0
 */
export default class EmptyProvisioningTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.EmptyProvisioningTypeEnum.${key}`);
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
      case this.EMPTY: {
        return 'default';
      }
      case this.NON_EMPTY: {
        return 'success';
      }
      case this.NOT_PROCESSED: {
        return 'info';
      }
      default: {
        return 'danger';
      }
    }
  }
}

EmptyProvisioningTypeEnum.EMPTY = Symbol('EMPTY');
EmptyProvisioningTypeEnum.NON_EMPTY = Symbol('NON_EMPTY');
EmptyProvisioningTypeEnum.NOT_PROCESSED = Symbol('NOT_PROCESSED');
