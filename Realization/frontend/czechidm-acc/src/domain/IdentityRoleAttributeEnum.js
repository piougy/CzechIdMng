import { Enums } from 'czechidm-core';

/**
 * Keys of Role fields
 */
export default class IdentityRoleAttributeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.IdentityRoleAttributeEnum.${key}`);
  }

  static getHelpBlockLabel(key) {
    return super.getNiceLabel(`acc:enums.IdentityRoleAttributeEnum.helpBlock.${key}`);
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
      case this.IDENTITY_CONTRACT: {
        return 'identityContract';
      }
      case this.ROLE: {
        return 'role';
      }
      case this.VALID_FROM: {
        return 'validFrom';
      }
      case this.VALID_TILL: {
        return 'validTill';
      }
      case this.DIRECT_ROLE: {
        return 'directRole';
      }
      case this.ROLE_COMPOSITION: {
        return 'roleComposition';
      }
      case this.CONTRACT_POSITION: {
        return 'contractPosition';
      }
      case this.EXTERNAL_ID: {
        return 'externalId';
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
      case 'identityContract': {
        return this.IDENTITY_CONTRACT;
      }
      case 'role': {
        return this.ROLE;
      }
      case 'validFrom': {
        return this.VALID_FROM;
      }
      case 'validTill': {
        return this.VALID_TILL;
      }
      case 'directRole': {
        return this.DIRECT_ROLE;
      }
      case 'roleComposition': {
        return this.ROLE_COMPOSITION;
      }
      case 'contractPosition': {
        return this.CONTRACT_POSITION;
      }
      case 'externalId': {
        return this.EXTERNAL_ID;
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

IdentityRoleAttributeEnum.IDENTITY_CONTRACT = Symbol('IDENTITY_CONTRACT');
IdentityRoleAttributeEnum.ROLE = Symbol('ROLE');
IdentityRoleAttributeEnum.VALID_FROM = Symbol('VALID_FROM');
IdentityRoleAttributeEnum.VALID_TILL = Symbol('VALID_TILL');
IdentityRoleAttributeEnum.ROLE_COMPOSITION = Symbol('ROLE_COMPOSITION');
IdentityRoleAttributeEnum.CONTRACT_POSITION = Symbol('CONTRACT_POSITION');
IdentityRoleAttributeEnum.EXTERNAL_ID = Symbol('EXTERNAL_ID');
