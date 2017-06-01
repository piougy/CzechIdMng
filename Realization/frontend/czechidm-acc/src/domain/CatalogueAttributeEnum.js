import { Enums } from 'czechidm-core';

/**
 * Keys of Role Catalogue fields
 */
export default class CatalogueAttributeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.CatalogueAttributeEnum.${key}`);
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
      case this.DESCRIPTION: {
        return 'description';
      }
      case this.URL: {
        return 'url';
      }
      case this.URL_TITLE: {
        return 'urlTitle';
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
      case 'description': {
        return this.DESCRIPTION;
      }
      case 'url': {
        return this.URL;
      }
      case 'urlTitle': {
        return this.URL_TITLE;
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

CatalogueAttributeEnum.CODE = Symbol('CODE');
CatalogueAttributeEnum.NAME = Symbol('NAME');
CatalogueAttributeEnum.PARENT = Symbol('PARENT');
CatalogueAttributeEnum.URL = Symbol('URL');
CatalogueAttributeEnum.URL_TITLE = Symbol('URL_TITLE');
CatalogueAttributeEnum.DESCRIPTION = Symbol('DESCRIPTION');
