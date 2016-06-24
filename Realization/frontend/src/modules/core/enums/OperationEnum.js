'use strict';

import AbstractEnum from '../../../modules/core/enums/AbstractEnum';

/**
 * OperationType for adit operation etc.
 */
export default class OperationEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.OperationEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }
}

OperationEnum.SECURITY = Symbol('SECURITY');
OperationEnum.FUNCTION_SECURITY = Symbol('FUNCTION_SECURITY');
OperationEnum.FUNCTION_RESULT = Symbol('FUNCTION_RESULT');
OperationEnum.WORKFLOW_START = Symbol('WORKFLOW_START');
OperationEnum.FUNCTION_INFO = Symbol('FUNCTION_INFO');
OperationEnum.CUSTOM = Symbol('CUSTOM');
