import { Enums } from 'czechidm-core';

/**
 * OperationResultType for synchronization.
 */
export default class OperationResultTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.OperationResultTypeEnum.${key}`);
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
      case this.SUCCESS: {
        return 'success';
      }
      case this.ERROR: {
        return 'danger';
      }
      case this.WARNING: {
        return 'warning';
      }
      case this.WF: {
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

OperationResultTypeEnum.SUCCESS = Symbol('SUCCESS');
OperationResultTypeEnum.ERROR = Symbol('ERROR');
OperationResultTypeEnum.WARNING = Symbol('WARNING');
OperationResultTypeEnum.WF = Symbol('WF');
OperationResultTypeEnum.IGNORE = Symbol('IGNORE');
