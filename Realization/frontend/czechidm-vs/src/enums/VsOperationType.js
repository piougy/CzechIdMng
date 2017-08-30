
import { Enums } from 'czechidm-core';
/**
 * Type of request on virtual system
 *
 * @author Vít Švanda
 */
export default class VsOperationType extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`vs:enums.VsOperationType.${key}`);
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
      case this.CREATE: {
        return 'success';
      }
      case this.UPDATE: {
        return 'success';
      }
      case this.DELETE: {
        return 'danger';
      }
      case this.DISABLE: {
        return 'warning';
      }
      case this.ENABLE: {
        return 'success';
      }
      case this.RESET_PASSWORD: {
        return 'primary';
      }
      default: {
        return 'default';
      }
    }
  }
}

VsOperationType.CREATE = Symbol('CREATE');
VsOperationType.UPDATE = Symbol('UPDATE');
VsOperationType.DELETE = Symbol('DELETE');
VsOperationType.DISABLE = Symbol('DISABLE');
VsOperationType.ENABLE = Symbol('ENABLE');
VsOperationType.RESET_PASSWORD = Symbol('RESET_PASSWORD');
