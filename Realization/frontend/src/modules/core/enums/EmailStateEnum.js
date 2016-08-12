

import AbstractEnum from '../../../modules/core/enums/AbstractEnum';

/**
 * Emails status enum
 * STATES:
 * - ALL
 * - NOT
 */
export default class EmailStateEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.EmailStateEnum.${key}`);
  }

  static getNiceLabelBySymbol(sym) {
    return this.getNiceLabel(this.findKeyBySymbol(sym));
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
      case this.ALL: {
        return 'success';
      }
      case this.NOT: {
        return 'danger';
      }
      default: {
        return 'default';
      }
    }
  }
}

EmailStateEnum.ALL = Symbol('ALL');
EmailStateEnum.NOT = Symbol('NOT');
