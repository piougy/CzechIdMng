

import AbstractEnum from '../../../modules/core/enums/AbstractEnum';

export default class CrtIdentityRoleStateEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`crt:enums.CrtIdentityRoleStateEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return AbstractEnum.findKeyBySymbol(this, sym);
  }
}

CrtIdentityRoleStateEnum.ASK  = Symbol('ASK');
CrtIdentityRoleStateEnum.WILL_ASK  = Symbol('WILL_ASK');
CrtIdentityRoleStateEnum.CANCEL_ASK  = Symbol('CANCEL_ASK');
CrtIdentityRoleStateEnum.WAIT_TO_APPROVE  = Symbol('WAIT_TO_APPROVE');
CrtIdentityRoleStateEnum.APPROVED  = Symbol('APPROVED');
