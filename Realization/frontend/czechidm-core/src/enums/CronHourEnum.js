import AbstractEnum from './AbstractEnum';

/**
 * Cron hour repetition possibilities - 1, 2, 3, 4, 6, 8, 12
 *
 * @author Petr Hanák
 */
export default class CronHourEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.CronHourEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }
}

CronHourEnum.ONE = Symbol('1');
CronHourEnum.TWO = Symbol('2');
CronHourEnum.THREE = Symbol('3');
CronHourEnum.FOUR = Symbol('4');
CronHourEnum.SIX = Symbol('6');
CronHourEnum.EIGHT = Symbol('8');
CronHourEnum.TWELVE = Symbol('12');
