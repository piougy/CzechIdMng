import AbstractEnum from './AbstractEnum';

/**
 * Two factor authentication method.
 *
 * @author Radek Tomiška
 * @since 10.6.0
 */
export default class TwoFactorAuthenticationTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.TwoFactorAuthenticationTypeEnum.${key}`);
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
      case this.APPLICATION: {
        return 'success';
      }
      case this.NOTIFICATION: {
        return 'info';
      }
      default: {
        return 'default';
      }
    }
  }
}

TwoFactorAuthenticationTypeEnum.APPLICATION = Symbol('APPLICATION');
TwoFactorAuthenticationTypeEnum.NOTIFICATION = Symbol('NOTIFICATION');
