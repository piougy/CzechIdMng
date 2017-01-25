import AbstractEnum from '../enums/AbstractEnum';

/**
 * Script category enums.
 * More information see BE class IdmScriptCategory.java
 * with all definition.
 */
export default class ScriptCategoryEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.ScriptCategoryEnum.${key}`);
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
    // TODO? Add some label like in role type?
    return 'default';
  }
}

ScriptCategoryEnum.DEFAULT = Symbol('DEFAULT');
ScriptCategoryEnum.TRANSFORM_FROM = Symbol('TRANSFORM_FROM');
ScriptCategoryEnum.TRANSFORM_TO = Symbol('TRANSFORM_TO');
ScriptCategoryEnum.SYSTEM = Symbol('SYSTEM');
