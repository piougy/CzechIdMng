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
}

CronMinuteEnum.ONE = Symbol('1');
CronMinuteEnum.TWO = Symbol('2');
CronMinuteEnum.THREE = Symbol('3');
CronMinuteEnum.FIVE = Symbol('5');
// CronMinuteEnum.SIX = Symbol('6');
CronMinuteEnum.TEN = Symbol('10');
// CronMinuteEnum.TWELVE = Symbol('12');
CronMinuteEnum.FIFTEEN = Symbol('15');
CronMinuteEnum.TWENTY = Symbol('20');
CronMinuteEnum.THIRTY = Symbol('30');
