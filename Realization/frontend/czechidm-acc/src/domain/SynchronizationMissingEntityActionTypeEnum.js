import { Enums } from 'czechidm-core';

/**
 * SynchronizationMissingEntityActionType for synchronization.
 */
export default class SynchronizationMissingEntityActionTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.SynchronizationMissingEntityActionTypeEnum.${key}`);
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
      case this.CREATE_ENTITY: {
        return 'success';
      }
      case this.DELETE_ACCOUNT: {
        return 'danger';
      }
      case this.DISABLE_ACCOUNT: {
        return 'warning';
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

SynchronizationMissingEntityActionTypeEnum.CREATE_ENTITY = Symbol('CREATE_ENTITY');
SynchronizationMissingEntityActionTypeEnum.DELETE_ACCOUNT = Symbol('DELETE_ACCOUNT');
SynchronizationMissingEntityActionTypeEnum.DISABLE_ACCOUNT = Symbol('DISABLE_ACCOUNT');
SynchronizationMissingEntityActionTypeEnum.IGNORE = Symbol('IGNORE');
