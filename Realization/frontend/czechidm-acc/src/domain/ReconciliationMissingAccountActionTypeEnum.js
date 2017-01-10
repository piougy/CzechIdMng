import { Enums } from 'czechidm-core';

/**
 * ReconciliationMissingAccountActionType for reconciliation.
 */
export default class ReconciliationMissingAccountActionTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.ReconciliationMissingAccountActionTypeEnum.${key}`);
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
      case this.CREATE_ACCOUNT: {
        return 'success';
      }
      case this.DELETE_ENTITY: {
        return 'danger';
      }
      case this.IGNORE: {
        return 'primary';
      }
      default: {
        return 'default';
      }
    }
  }
}

ReconciliationMissingAccountActionTypeEnum.CREATE_ACCOUNT = Symbol('CREATE_ACCOUNT');
ReconciliationMissingAccountActionTypeEnum.DELETE_ENTITY = Symbol('DELETE_ENTITY');
ReconciliationMissingAccountActionTypeEnum.IGNORE = Symbol('IGNORE');
