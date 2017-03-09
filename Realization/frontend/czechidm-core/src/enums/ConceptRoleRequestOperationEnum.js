
import AbstractEnum from '../enums/AbstractEnum';

/**
 * Role request operation enum
 */
export default class ConceptRoleRequestStateEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.ConceptRoleRequestStateEnum.${key}`);
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
      case this.ADD: {
        return 'success';
      }
      case this.UPDATE: {
        return 'warning';
      }
      case this.REMOVE: {
        return 'danger';
      }
      default: {
        return 'default';
      }
    }
  }
}

ConceptRoleRequestStateEnum.ADD = Symbol('ADD');
ConceptRoleRequestStateEnum.UPDATE = Symbol('UPDATE');
ConceptRoleRequestStateEnum.REMOVE = Symbol('REMOVE');
