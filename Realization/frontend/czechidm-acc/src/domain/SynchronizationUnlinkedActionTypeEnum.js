import { Enums } from 'czechidm-core';

/**
 * SynchronizationUnlinkedActionTypeEnum for synchronization.
 */
export default class SynchronizationUnlinkedActionTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.SynchronizationUnlinkedActionTypeEnum.${key}`);
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
      case this.LINK_AND_UPDATE_ACCOUNT: {
        return 'success';
      }
      case this.LINK_AND_UPDATE_ENTITY: {
        return 'success';
      }
      case this.LINK: {
        return 'success';
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

SynchronizationUnlinkedActionTypeEnum.LINK_AND_UPDATE_ENTITY = Symbol('LINK_AND_UPDATE_ENTITY');
SynchronizationUnlinkedActionTypeEnum.LINK_AND_UPDATE_ACCOUNT = Symbol('LINK_AND_UPDATE_ACCOUNT');
SynchronizationUnlinkedActionTypeEnum.LINK = Symbol('LINK');
SynchronizationUnlinkedActionTypeEnum.IGNORE = Symbol('IGNORE');
