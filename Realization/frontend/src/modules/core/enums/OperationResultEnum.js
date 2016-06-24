'use strict';

import AbstractEnum from '../../../modules/core/enums/AbstractEnum';

/**
 * OperationType for adit operation etc.
 */
export default class OperationResultEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.OperationResultEnum.${key}`);
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
      case this.SUCCESS: {
        return 'success';
      }
      case this.FAILED: {
        return 'danger';
      }
    }
  }
}

OperationResultEnum.SUCCESS = Symbol('SUCCESS');
OperationResultEnum.FAILED = Symbol('FAILED');
