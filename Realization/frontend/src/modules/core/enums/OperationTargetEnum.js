

import AbstractEnum from '../../../modules/core/enums/AbstractEnum';

/**
 * OperationType for adit operation etc.
 */
export default class OperationTargetEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.OperationTargetEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }
}

OperationTargetEnum.ACCOUNT = Symbol('ACCOUNT');
OperationTargetEnum.APPROVAL = Symbol('APPROVAL');
OperationTargetEnum.ATTRIBUTE = Symbol('ATTRIBUTE');
OperationTargetEnum.CONTROLED_ORGANISATION = Symbol('CONTROLED_ORGANISATION');
OperationTargetEnum.DENIED_ORGANISATION = Symbol('DENIED_ORGANISATION');
OperationTargetEnum.IDENTITY = Symbol('IDENTITY');
OperationTargetEnum.IDENTIFIER = Symbol('IDENTIFIER');
OperationTargetEnum.ENTITY = Symbol('ENTITY');
OperationTargetEnum.MANAGER = Symbol('MANAGER');
OperationTargetEnum.HOME_ORGANISATION = Symbol('HOME_ORGANISATION');
OperationTargetEnum.PASSWORD = Symbol('PASSWORD');
OperationTargetEnum.DELEGATION = Symbol('DELEGATION');
OperationTargetEnum.ROLE = Symbol('ROLE');
OperationTargetEnum.ROLE_DETAIL = Symbol('ROLE_DETAIL');
OperationTargetEnum.ADMIN_ROLE = Symbol('ADMIN_ROLE');
OperationTargetEnum.PRIVILEGE = Symbol('PRIVILEGE');
OperationTargetEnum.SUB_ROLES = Symbol('SUB_ROLES');
OperationTargetEnum.USERTASK = Symbol('USERTASK');
OperationTargetEnum.TOP_MODULE = Symbol('TOP_MODULE');
OperationTargetEnum.MODULE_NODE = Symbol('MODULE_NODE');
