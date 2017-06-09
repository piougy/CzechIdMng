

import AbstractEnum from '../enums/AbstractEnum';

/**
 * OperationType for adit operation etc.
 */
export default class PasswordPolicyIdentityAttributeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.PasswordPolicyIdentityAttributeEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }
}

PasswordPolicyIdentityAttributeEnum.USERNAME = Symbol('USERNAME');
PasswordPolicyIdentityAttributeEnum.EMAIL = Symbol('EMAIL');
PasswordPolicyIdentityAttributeEnum.FIRSTNAME = Symbol('FIRSTNAME');
PasswordPolicyIdentityAttributeEnum.LASTNAME = Symbol('LASTNAME');
