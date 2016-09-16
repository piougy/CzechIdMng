

import AbstractEnum from '../enums/AbstractEnum';

/**
 * OperationType for adit operation etc.
 */
export default class OperationTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.OperationTypeEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }
}

OperationTypeEnum.ADD = Symbol('ADD');
OperationTypeEnum.APPROVE = Symbol('APPROVE');
OperationTypeEnum.CHANGE = Symbol('CHANGE');
OperationTypeEnum.CREATE = Symbol('CREATE');
OperationTypeEnum.DENY = Symbol('DENY');
OperationTypeEnum.DISABLE = Symbol('DISABLE');
OperationTypeEnum.ENABLE = Symbol('ENABLE');
OperationTypeEnum.MOVE = Symbol('MOVE');
OperationTypeEnum.REMOVE = Symbol('REMOVE');
OperationTypeEnum.RENAME = Symbol('RENAME');
OperationTypeEnum.SET = Symbol('SET');
OperationTypeEnum.UNLINK = Symbol('UNLINK');
OperationTypeEnum.UPDATE = Symbol('UPDATE');
OperationTypeEnum.WANT_ADD = Symbol('WANT_ADD');
