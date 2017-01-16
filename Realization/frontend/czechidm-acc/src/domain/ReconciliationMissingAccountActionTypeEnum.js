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
      case this.LINK: {
        return 'success';
      }
      case this.DELETE_ENTITY: {
        return 'danger';
      }
      case this.UNLINK: {
        return 'warning';
      }
      case this.LINK_AND_UPDATE_ACCOUNT: {
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

ReconciliationMissingAccountActionTypeEnum.LINK = Symbol('LINK');
ReconciliationMissingAccountActionTypeEnum.DELETE_ENTITY = Symbol('DELETE_ENTITY');
ReconciliationMissingAccountActionTypeEnum.UNLINK = Symbol('UNLINK');
ReconciliationMissingAccountActionTypeEnum.LINK_AND_UPDATE_ACCOUNT = Symbol('LINK_AND_UPDATE_ACCOUNT');
ReconciliationMissingAccountActionTypeEnum.IGNORE = Symbol('IGNORE');
