import { Enums } from 'czechidm-core';

/**
 * Keys of Role fields
 */
export default class RoleAttributeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.RoleAttributeEnum.${key}`);
  }

  static getHelpBlockLabel(key) {
    return super.getNiceLabel(`acc:enums.RoleAttributeEnum.helpBlock.${key}`);
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
      case this.NAME: {
        return 'name';
      }
      case this.BASE_CODE: {
        return 'baseCode';
      }
      case this.ENVIRONMENT: {
        return 'environment';
      }
      case this.ROLE_TYPE: {
        return 'roleType';
      }
      case this.PRIORITY: {
        return 'priority';
      }
      case this.APPROVE_REMOVE: {
        return 'approveRemove';
      }
      case this.DESCRIPTION: {
        return 'description';
      }
      case this.DISABLED: {
        return 'disabled';
      }
      case this.ROLE_MEMBERSHIP_ID: {
        return 'roleMembershipId';
      }
      case this.ROLE_FORWARD_ACM: {
        return 'roleForwardAcm';
      }
      case this.ROLE_SKIP_VALUE_IF_EXCLUDED: {
        return 'roleSkipValueIfExcluded';
      }
      case this.ROLE_MEMBERS_FIELD: {
        return 'roleMembers';
      }
      case this.ROLE_CATALOGUE_FIELD: {
        return 'roleCatalogue';
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
      case 'name': {
        return this.NAME;
      }
      case 'baseCode': {
        return this.BASE_CODE;
      }
      case 'environment': {
        return this.ENVIRONMENT;
      }
      case 'roleType': {
        return this.ROLE_TYPE;
      }
      case 'priority': {
        return this.PRIORITY;
      }
      case 'approveRemove': {
        return this.APPROVE_REMOVE;
      }
      case 'description': {
        return this.DESCRIPTION;
      }
      case 'disabled': {
        return this.DISABLED;
      }
      case 'roleMembershipId': {
        return this.ROLE_MEMBERSHIP_ID;
      }
      case 'roleForwardAcm': {
        return this.ROLE_FORWARD_ACM;
      }
      case 'roleSkipValueIfExcluded': {
        return this.ROLE_SKIP_VALUE_IF_EXCLUDED;
      }
      case 'roleMembers': {
        return this.ROLE_MEMBERS_FIELD;
      }
      case 'roleCatalogue': {
        return this.ROLE_CATALOGUE_FIELD;
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

RoleAttributeEnum.NAME = Symbol('NAME');
RoleAttributeEnum.BASE_CODE = Symbol('BASE_CODE');
RoleAttributeEnum.ENVIRONMENT = Symbol('ENVIRONMENT');
RoleAttributeEnum.ROLE_TYPE = Symbol('ROLE_TYPE');
RoleAttributeEnum.PRIORITY = Symbol('PRIORITY');
RoleAttributeEnum.APPROVE_REMOVE = Symbol('APPROVE_REMOVE');
RoleAttributeEnum.DESCRIPTION = Symbol('DESCRIPTION');
RoleAttributeEnum.DISABLED = Symbol('DISABLED');
RoleAttributeEnum.ROLE_MEMBERSHIP_ID = Symbol('ROLE_MEMBERSHIP_ID');
RoleAttributeEnum.ROLE_FORWARD_ACM = Symbol('ROLE_FORWARD_ACM');
RoleAttributeEnum.ROLE_SKIP_VALUE_IF_EXCLUDED = Symbol('ROLE_SKIP_VALUE_IF_EXCLUDED');
RoleAttributeEnum.ROLE_MEMBERS_FIELD = Symbol('ROLE_MEMBERS_FIELD');
RoleAttributeEnum.ROLE_CATALOGUE_FIELD = Symbol('ROLE_CATALOGUE_FIELD');
