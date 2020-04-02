import AbstractEnum from './AbstractEnum';

/**
 * Cron hour repetition possibilities - 1, 2, 3, 4, 6, 8, 12
 *
 * @author Petr Hanák
 */
export default class CronHourEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.CronHourEnum.${ key }`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }

  static getValue(key) {
    const sym = super.findSymbolByKey(this, key);
    //
    switch (sym) {
      case this.ONE: {
        return 1;
      }
      case this.TWO: {
        return 2;
      }
      case this.THREE: {
        return 3;
      }
      case this.FOUR: {
        return 4;
      }
      case this.SIX: {
        return 6;
      }
      case this.EIGHT: {
        return 8;
      }
      case this.TWELVE: {
        return 8;
      }
      default: {
        return null;
      }
    }
  }
}

CronHourEnum.ONE = Symbol('ONE');
CronHourEnum.TWO = Symbol('TWO');
CronHourEnum.THREE = Symbol('THREE');
CronHourEnum.FOUR = Symbol('FOUR');
CronHourEnum.SIX = Symbol('SIX');
CronHourEnum.EIGHT = Symbol('EIGHT');
CronHourEnum.TWELVE = Symbol('TWELVE');
