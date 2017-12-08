import AbstractEnum from '../enums/AbstractEnum';

/**
 * Identity state
 *
 * @author Radek Tomiška
 */
export default class IdentityStateEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.IdentityStateEnum.${key}`);
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
    //
    const sym = super.findSymbolByKey(this, key);
    switch (sym) {
      case this.VALID: {
        return 'success';
      }
      case this.DISABLED_MANUALLY: {
        return 'danger';
      }
      case this.CREATED:
      case this.FUTURE_CONTRACT:
      case this.STARTING_CONTRACT: {
        return 'info';
      }
      default: {
        return 'default';
      }
    }
  }

  /**
   * State leeds to disabled contract
   *
   * @param  {string} key
   * @return {Boolean}
   */
  static isIdentityDisabled(key) {
    const sym = super.findSymbolByKey(this, key);
    //
    switch (sym) {
      case this.VALID: {
        return false;
      }
      default: {
        return true;
      }
    }
  }
}

IdentityStateEnum.CREATED = Symbol('CREATED');
IdentityStateEnum.NO_CONTRACT = Symbol('NO_CONTRACT');
IdentityStateEnum.FUTURE_CONTRACT = Symbol('FUTURE_CONTRACT');
IdentityStateEnum.VALID = Symbol('VALID');
// IdentityStateEnum.PASSWORD_EXPIRED = Symbol('PASSWORD_EXPIRED');
IdentityStateEnum.LEFT = Symbol('LEFT');
IdentityStateEnum.DISABLED = Symbol('DISABLED');
IdentityStateEnum.DISABLED_MANUALLY = Symbol('DISABLED_MANUALLY');
