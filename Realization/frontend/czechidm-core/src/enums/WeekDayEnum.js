import AbstractEnum from './AbstractEnum';

/**
 * Day in week - monday, tuesday, wednesday..
 *
 * @author Petr Hanák
 */
export default class WeekDayEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.WeekDayEnum.${key}`);
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
      case this.MONDAY: {
        return 'mon';
      }
      case this.TUESDAY: {
        return 'tue';
      }
      case this.WEDNESDAY: {
        return 'wed';
      }
      case this.THURSTDAY: {
        return 'thu';
      }
      case this.FRIDAY: {
        return 'fri';
      }
      case this.SATURDAY: {
        return 'sat';
      }
      case this.SUNDAY: {
        return 'sun';
      }
      default: {
        return null;
      }
    }
  }
}

WeekDayEnum.MONDAY = Symbol('MONDAY');
WeekDayEnum.TUESDAY = Symbol('TUESDAY');
WeekDayEnum.WEDNESDAY = Symbol('WEDNESDAY');
WeekDayEnum.THURSTDAY = Symbol('THURSTDAY');
WeekDayEnum.FRIDAY = Symbol('FRIDAY');
WeekDayEnum.SATURDAY = Symbol('SATURDAY');
WeekDayEnum.SUNDAY = Symbol('SUNDAY');
