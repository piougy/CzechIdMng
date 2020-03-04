import AbstractEnum from './AbstractEnum';

/**
 * Cron minute repetition possibilities - 1, 2, 3, 5, 6, 10, 12, 15, 20, 30
 *
 * @author Petr Hanák
 */
export default class CronMinuteEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.CronMinuteEnum.${key}`);
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
      case this.FIVE: {
        return 5;
      }
      case this.TEN: {
        return 10;
      }
      case this.FIFTEEN: {
        return 15;
      }
      case this.TWENTY: {
        return 20;
      }
      case this.THIRTY: {
        return 30;
      }
      default: {
        return null;
      }
    }
  }
}

CronMinuteEnum.ONE = Symbol('ONE');
CronMinuteEnum.TWO = Symbol('TWO');
CronMinuteEnum.THREE = Symbol('THREE');
CronMinuteEnum.FIVE = Symbol('FIVE');
// CronMinuteEnum.SIX = Symbol('6');
CronMinuteEnum.TEN = Symbol('TEN');
// CronMinuteEnum.TWELVE = Symbol('12');
CronMinuteEnum.FIFTEEN = Symbol('FIFTEEN');
CronMinuteEnum.TWENTY = Symbol('TWENTY');
CronMinuteEnum.THIRTY = Symbol('THIRTY');
