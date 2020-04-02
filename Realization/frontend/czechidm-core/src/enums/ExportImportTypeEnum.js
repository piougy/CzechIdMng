import AbstractEnum from './AbstractEnum';

/**
 * Export-import typem
 *
 * @author Vít Švanda
 */
export default class ExportImportTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.ExportImportEnum.${key}`);
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
      case this.EXPORT: {
        return 'primary';
      }
      case this.IMPORT: {
        return 'success';
      }
      default: {
        return 'warning';
      }
    }
  }
}

ExportImportTypeEnum.EXPORT = Symbol('EXPORT');
ExportImportTypeEnum.IMPORT = Symbol('IMPORT');
