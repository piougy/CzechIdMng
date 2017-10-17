import { Enums } from 'czechidm-core';

/**
 * Keys of contract fields
 */
export default class ContractAttributeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.ContractAttributeEnum.${key}`);
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
      case this.IDENTITY: {
        return 'identity';
      }
      case this.VALID_FROM: {
        return 'validFrom';
      }
      case this.VALID_TILL: {
        return 'validTill';
      }
      case this.WORK_POSITION: {
        return 'workPosition';
      }
      case this.POSITION: {
        return 'position';
      }
      case this.EXTERNE: {
        return 'externe';
      }
      case this.MAIN: {
        return 'main';
      }
      case this.DESCRIPTION: {
        return 'description';
      }
      case this.GUARANTESS: {
        return 'guarantees';
      }
      case this.STATE: {
        return 'state';
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
      case 'identity': {
        return this.IDENTITY;
      }
      case 'validFrom': {
        return this.VALID_FROM;
      }
      case 'validTill': {
        return this.VALID_TILL;
      }
      case 'workPosition': {
        return this.WORK_POSITION;
      }
      case 'position': {
        return this.POSITION;
      }
      case 'externe': {
        return this.EXTERNE;
      }
      case 'main': {
        return this.MAIN;
      }
      case 'description': {
        return this.DESCRIPTION;
      }
      case 'disabled': {
        return this.DISABLED;
      }
      case 'guarantees': {
        return this.GUARANTESS;
      }
      case 'state': {
        return this.STATE;
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

ContractAttributeEnum.IDENTITY = Symbol('IDENTITY');
ContractAttributeEnum.MAIN = Symbol('MAIN');
ContractAttributeEnum.STATE = Symbol('STATE');
ContractAttributeEnum.POSITION = Symbol('POSITION');
ContractAttributeEnum.WORK_POSITION = Symbol('WORK_POSITION');
ContractAttributeEnum.VALID_FROM = Symbol('VALID_FROM');
ContractAttributeEnum.VALID_TILL = Symbol('VALID_TILL');
ContractAttributeEnum.EXTERNE = Symbol('EXTERNE');
ContractAttributeEnum.DESCRIPTION = Symbol('DESCRIPTION');
ContractAttributeEnum.DISABLED = Symbol('DISABLED');
ContractAttributeEnum.GUARANTESS = Symbol('GUARANTESS');
