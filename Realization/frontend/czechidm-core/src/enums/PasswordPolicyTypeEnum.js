import AbstractEnum from '../enums/AbstractEnum';

/**
 * Password policy type enums.
 */
export default class PasswordPolicyTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.PasswordPolicyTypeEnum.${key}`);
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
      case this.VALIDATE: {
        return 'primary';
      }
      case this.GENERATE: {
        return 'success';
      }
      default: {
        return 'default';
      }
    }
  }
}

PasswordPolicyTypeEnum.VALIDATE = Symbol('VALIDATE');
PasswordPolicyTypeEnum.GENERATE = Symbol('GENERATE');
