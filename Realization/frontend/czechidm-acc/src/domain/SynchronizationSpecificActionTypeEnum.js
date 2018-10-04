import { Enums } from 'czechidm-core';

/**
 * SynchronizationSpecificActionTypeEnum for synchronization.
 */
export default class SynchronizationSpecificActionTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.SynchronizationSpecificActionTypeEnum.${key}`);
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
      case this.DO_NOT_LINK: {
        return 'success';
      }
      case this.LINK_PROTECTED: {
        return 'success';
      }
      case this.LINK: {
        return 'success';
      }
      default: {
        return 'default';
      }
    }
  }
}

SynchronizationSpecificActionTypeEnum.DO_NOT_LINK = Symbol('DO_NOT_LINK');
SynchronizationSpecificActionTypeEnum.LINK_PROTECTED = Symbol('LINK_PROTECTED');
SynchronizationSpecificActionTypeEnum.LINK = Symbol('LINK');
