

import AbstractEnum from '../../../modules/core/enums/AbstractEnum';

/**
 * OperationType for audit operation etc.
 */
export default class ObjectClassEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.ObjectClassEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }
}

ObjectClassEnum.Identity = Symbol('Identity');
ObjectClassEnum.Role = Symbol('Role');
ObjectClassEnum.Organisation = Symbol('Organisation');
ObjectClassEnum.Resource = Symbol('Resource');
ObjectClassEnum.WorkflowDefinition = Symbol('WorkflowDefinition');
