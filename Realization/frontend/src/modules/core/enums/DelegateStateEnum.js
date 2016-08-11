import AbstractEnum from '../../../modules/core/enums/AbstractEnum';

/**
 * Delegate setting
 */
export default class DelegateStateEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.DelegateStateEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.APPROVED: {
        return 'success';
      }
      case this.DENIED: {
        return 'danger';
      }
      default: {
        return 'warning';
      }
    }
  }
}

DelegateStateEnum.PENDING = Symbol('PENDING');
DelegateStateEnum.DENIED = Symbol('DENIED');
DelegateStateEnum.APPROVED = Symbol('APPROVED');
