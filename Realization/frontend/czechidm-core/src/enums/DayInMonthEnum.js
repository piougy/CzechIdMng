import AbstractEnum from './AbstractEnum';

/**
 * Cron day in month repetition possibilities 1 - 31
 *
 * @author Petr Hanák
 */
export default class DayInMonthEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.DayInMonthEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }
}

DayInMonthEnum.ONE = Symbol('1');
DayInMonthEnum.TWO = Symbol('2');
DayInMonthEnum.THREE = Symbol('3');
DayInMonthEnum.FOUR = Symbol('4');
DayInMonthEnum.FIVE = Symbol('5');
DayInMonthEnum.SIX = Symbol('6');
DayInMonthEnum.SEVEN = Symbol('7');
DayInMonthEnum.EIGHT = Symbol('8');
DayInMonthEnum.NINE = Symbol('9');
DayInMonthEnum.TEN = Symbol('10');
DayInMonthEnum.ELEVEN = Symbol('11');
DayInMonthEnum.TWELVE = Symbol('12');
DayInMonthEnum.THIRTEEN = Symbol('13');
DayInMonthEnum.FOURTEEN = Symbol('14');
DayInMonthEnum.FIFTEEN = Symbol('15');
DayInMonthEnum.SIXTEEN = Symbol('16');
DayInMonthEnum.SEVENTEEN = Symbol('17');
DayInMonthEnum.EIGHTEEN = Symbol('18');
DayInMonthEnum.NINETEEN = Symbol('19');
DayInMonthEnum.TWENTY = Symbol('20');
DayInMonthEnum.TWENTYONE = Symbol('21');
DayInMonthEnum.TWENTYTWO = Symbol('22');
DayInMonthEnum.TWENTYTHREE = Symbol('23');
DayInMonthEnum.TWENTYFOUR = Symbol('24');
DayInMonthEnum.TWENTYFIVE = Symbol('25');
DayInMonthEnum.TWENTYSIX = Symbol('26');
DayInMonthEnum.TWENTYSEVEN = Symbol('27');
DayInMonthEnum.TWENTYEIGHT = Symbol('28');
DayInMonthEnum.TWENTYNINE = Symbol('29');
DayInMonthEnum.THIRTY = Symbol('30');
DayInMonthEnum.THIRTYONE = Symbol('31');
