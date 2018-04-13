import AbstractEnum from '../enums/AbstractEnum';

/**
 * Keys of contract fields
 */
export default class ContractSliceAttributeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.ContractSliceAttributeEnum.${key}`);
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

ContractSliceAttributeEnum.IDENTITY = Symbol('IDENTITY');
ContractSliceAttributeEnum.MAIN = Symbol('MAIN');
ContractSliceAttributeEnum.STATE = Symbol('STATE');
ContractSliceAttributeEnum.POSITION = Symbol('POSITION');
ContractSliceAttributeEnum.WORK_POSITION = Symbol('WORK_POSITION');
ContractSliceAttributeEnum.VALID_FROM = Symbol('VALID_FROM');
ContractSliceAttributeEnum.VALID_TILL = Symbol('VALID_TILL');
ContractSliceAttributeEnum.EXTERNE = Symbol('EXTERNE');
ContractSliceAttributeEnum.DESCRIPTION = Symbol('DESCRIPTION');
ContractSliceAttributeEnum.GUARANTESS = Symbol('GUARANTESS');
