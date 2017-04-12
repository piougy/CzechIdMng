import { Enums } from 'czechidm-core';

/**
 * Keys of Tree fields
 */
export default class TreeAttributeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.TreeAttributeEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }

  static getField(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.CODE: {
        return 'code';
      }
      case this.NAME: {
        return 'name';
      }
      case this.PARENT: {
        return 'parent';
      }
      case this.EXTERNAL_ID: {
        return 'externalId';
      }
      case this.DISABLED: {
        return 'disabled';
      }
      default: {
        return null;
      }
    }
  }

  static getEnum(field) {
    if (!field) {
      return null;
    }

    switch (field) {
      case 'code': {
        return this.CODE;
      }
      case 'name': {
        return this.NAME;
      }
      case 'parent': {
        return this.PARENT;
      }
      case 'externalId': {
        return this.EXTERNAL_ID;
      }
      case 'disabled': {
        return this.DISABLED;
      }
      default: {
        return null;
      }
    }
  }

  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      default: {
        return 'default';
      }
    }
  }
}

TreeAttributeEnum.CODE = Symbol('CODE');
TreeAttributeEnum.NAME = Symbol('NAME');
TreeAttributeEnum.PARENT = Symbol('PARENT');
TreeAttributeEnum.EXTERNAL_ID = Symbol('EXTERNAL_ID');
TreeAttributeEnum.DISABLED = Symbol('DISABLED');
