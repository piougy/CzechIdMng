
import { Enums } from 'czechidm-core';
/**
 * Type of value change on virtual system
 *
 * @author Vít Švanda
 */
export default class VsValueChangeType extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`vs:enums.VsValueChangeType.${key}`);
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
      case this.ADDED: {
        return 'success';
      }
      case this.UPDATED: {
        return 'warning';
      }
      case this.REMOVED: {
        return 'danger';
      }
      default: {
        return 'default';
      }
    }
  }
}

VsValueChangeType.ADDED = Symbol('ADDED');
VsValueChangeType.UPDATED = Symbol('UPDATED');
VsValueChangeType.REMOVED = Symbol('REMOVED');
