import React from 'react';
import moment from 'moment';
//
import * as Basic from '../../basic';
import AbstractFormComponent from '../../basic/AbstractFormComponent/AbstractFormComponent';
import IntervalTypeEnum from '../../../enums/IntervalTypeEnum';
import WeekDayEnum from '../../../enums/WeekDayEnum';
import CronMinuteEnum from '../../../enums/CronMinuteEnum';
import CronHourEnum from '../../../enums/CronHourEnum';

/**
 * Cron wizard used for repeatable trigger.
 *
 * FIXME: result cron is not show, when modal dialog is reopened => refactor whole recount cron mechism to be stateles (=> to can be called in constructor too).
 *
 * @author Petr Hanák
 * @author Radek Tomiška
 * @since 10.2.0
 */
export default class CronGenerator extends AbstractFormComponent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.MINUTE),
      weekDay: WeekDayEnum.findKeyBySymbol(WeekDayEnum.MONDAY),
      cronMinute: CronMinuteEnum.findKeyBySymbol(CronMinuteEnum.FIVE),
      cronHour: CronHourEnum.findKeyBySymbol(CronHourEnum.ONE),
      dayInMonth: 1,
      initTime: moment.now(),
      time: moment.now(),
      initExecuteDate: moment.now(),
      executeDate: moment.now(),
      showMonthWarning: false,
      weekDaySpelling: 'every_1'
    };
  }

  getComponentKey() {
    return 'entity.SchedulerTask.trigger.repeat';
  }

  onChangeIntervalType(intervalType) {
    this.setState({
      intervalType: intervalType.value,
      showMonthWarning: this.showWarning(intervalType.value)
    }, () => this.generateCron());
  }

  showWarning(intervalType) {
    const dayNumber = this.state.dayInMonth;
    //
    return (intervalType === 'MONTH' && (dayNumber > 28));
  }

  onChangeWeekDay(weekDay) {
    const weekDaySymbol = WeekDayEnum.findSymbolByKey(weekDay.value);
    this.setState({
      weekDay: weekDay.value,
      weekDaySpelling: this.checkWeekDaySpelling(weekDaySymbol)
    }, () => this.generateCron());
  }

  checkWeekDaySpelling(weekDaySymbol) {
    switch (weekDaySymbol) {
      case WeekDayEnum.MONDAY:
        return 'every_1';
      case WeekDayEnum.TUESDAY:
        return 'every_1';
      case WeekDayEnum.WEDNESDAY:
        return 'every_2';
      case WeekDayEnum.THURSTDAY:
        return 'every_3';
      case WeekDayEnum.FRIDAY:
        return 'every_3';
      case WeekDayEnum.SATURDAY:
        return 'every_2';
      case WeekDayEnum.SUNDAY:
        return 'every_2';
      default:
        return null;
    }
  }

  onChangeCronMinute(cronMinute) {
    this.setState({
      cronMinute: cronMinute.value
    }, () => this.generateCron());
  }

  onChangeCronHour(cronHour) {
    this.setState({
      cronHour: cronHour.value
    }, () => this.generateCron());
  }

  onChangeDayInMonth(value) {
    const dayNumber = value.value;
    this.setState({
      dayInMonth: dayNumber,
      showMonthWarning: (dayNumber > 28)
    }, () => this.generateCron());
  }

  onChangeTime(time) {
    this.setState({
      time
    }, () => this.generateCron());
  }

  onExecuteDateChange(executeDate) {
    this.setState({
      executeDate
    }, () => this.generateCron());
  }

  getExecuteDate() {
    return this.refs.executeDate.getValue();
  }

  generateCron() {
    this.setState({
      cronExpression: this.getCron()
    });
  }

  getCron() {
    const { intervalType } = this.state;
    switch (intervalType) {
      case 'MINUTE':
        return this.resolveMinutes();
      case 'HOUR':
        return this.resolveHours();
      case 'DAY':
        return this.resolveEveryDay();
      case 'WEEK':
        return this.resolveEveryWeek();
      case 'MONTH':
        return this.resolveEveryMonth();
      default:
        return null;
    }
  }

  reCronNumber(number) {
    // cuts first zeros from minutes and hours
    if (number && number.length === 2) {
      const result = (number[0] === '0') ? number[1] : number;
      return result;
    }
    return number;
  }

  resolveMinutes() {
    const { executeDate } = this.state;
    //
    const initMinute = executeDate ? moment(executeDate).get('minute') : 0;
    const repetitionRate = CronMinuteEnum.getValue(this.state.cronMinute);
    //
    let cronStartMinute = this.reCronNumber(initMinute);
    if (initMinute - repetitionRate > 0) {
      while (cronStartMinute - repetitionRate > 0) {
        cronStartMinute -= repetitionRate;
      }
    }

    const second = 0;
    const minute = `${cronStartMinute}/${repetitionRate}`;
    const hour = `*`;
    const monthDay = `*`;
    const monthInYear = `*`;
    const dayOfWeek = `?`;

    return `${second} ${minute} ${hour} ${monthDay} ${monthInYear} ${dayOfWeek}`;
  }

  resolveHours() {
    const { executeDate, cronHour } = this.state;
    const initHour = executeDate ? moment(executeDate).get('hour') : 0;
    const initMinute = executeDate ? moment(executeDate).get('minute') : 0;
    const repetitionRate = CronHourEnum.getValue(cronHour);

    let cronStartHour = this.reCronNumber(initHour);
    if (initHour - repetitionRate > 0) {
      while (cronStartHour - repetitionRate > 0) {
        cronStartHour -= repetitionRate;
      }
    }

    const second = 0;
    const minute = this.reCronNumber(initMinute);
    const hour = `${cronStartHour}/${repetitionRate}`;
    const monthDay = `*`;
    const monthInYear = `*`;
    const dayOfWeek = `?`;

    return `${ second } ${ minute } ${ hour } ${ monthDay } ${ monthInYear } ${ dayOfWeek }`;
  }

  resolveEveryDay() {
    const { executeDate } = this.state;
    //
    const initHour = executeDate ? moment(executeDate).get('hour') : 0;
    const initMinute = executeDate ? moment(executeDate).get('minute') : 0;
    const second = 0;
    const minute = this.reCronNumber(initMinute);
    const hour = this.reCronNumber(initHour);
    const monthDay = '*';
    const monthInYear = '*';
    const dayOfWeek = '?';

    return `${ second } ${ minute } ${ hour } ${ monthDay } ${ monthInYear } ${dayOfWeek}`;
  }

  resolveEveryWeek() {
    const { time, weekDay } = this.state;
    //
    const initHour = time ? moment(time).get('hour') : 0;
    const initMinute = time ? moment(time).get('minute') : 0;
    const second = 0;
    const minute = this.reCronNumber(initMinute);
    const hour = this.reCronNumber(initHour);
    const monthDay = '?';
    const monthInYear = '*';
    const dayOfWeek = WeekDayEnum.getValue(weekDay);

    return `${ second } ${ minute } ${ hour } ${ monthDay } ${ monthInYear } ${ dayOfWeek }`;
  }

  resolveEveryMonth() {
    const { time, dayInMonth } = this.state;
    //
    const initHour = time ? moment(time).get('hour') : 0;
    const initMinute = time ? moment(time).get('minute') : 0;
    const second = 0;
    const minute = this.reCronNumber(initMinute);
    const hour = this.reCronNumber(initHour);
    const monthDay = dayInMonth;
    const monthInYear = '*';
    const dayOfWeek = '?';

    return `${ second } ${ minute } ${ hour } ${ monthDay } ${ monthInYear } ${ dayOfWeek }`;
  }

  _getDayInMonthOptions() {
    const options = [];
    for (let i = 1; i <= 31; i++) {
      options.push({
        value: i,
        niceLabel: `${ i }.`
      });
    }
    //
    return options;
  }

  getBody() {
    const { showLoading, rendered } = this.props;
    const {
      intervalType,
      cronExpression,
      cronMinute,
      cronHour,
      weekDay,
      dayInMonth,
      showMonthWarning,
      weekDaySpelling,
      initTime,
      initExecuteDate
    } = this.state;
    if (!rendered) {
      return null;
    }
    if (showLoading) {
      return (
        <Basic.Loading isStatic showLoading/>
      );
    }
    return (
      <Basic.Div className="cron-generator">
        <Basic.DateTimePicker
          ref="executeDate"
          label={ this.i18n('entity.SchedulerTask.trigger.executeDate.label') }
          helpBlock={ this.i18n('entity.SchedulerTask.trigger.executeDate.help') }
          value={ initExecuteDate }
          onChange={ this.onExecuteDateChange.bind(this) }/>

        <Basic.LabelWrapper label={ this.i18n('repeatEvery') }>
          <div className="main-group">
            <Basic.EnumSelectBox
              ref="cronMinute"
              className="num-select"
              enum={ CronMinuteEnum }
              value={ cronMinute }
              clearable={ false }
              hidden={ intervalType !== 'MINUTE' }
              onChange={ this.onChangeCronMinute.bind(this) }/>
            <Basic.EnumSelectBox
              ref="cronHour"
              className="num-select"
              enum={ CronHourEnum }
              value={ cronHour }
              clearable={ false }
              hidden={ intervalType !== 'HOUR' }
              onChange={ this.onChangeCronHour.bind(this) }/>

            <Basic.EnumSelectBox
              ref="intervalType"
              className="interval-select"
              enum={ IntervalTypeEnum }
              value={ intervalType }
              clearable={ false }
              onChange={ this.onChangeIntervalType.bind(this) }
              input={ false }/>

            {/* Week properties */}
            { intervalType === 'WEEK' && (
              <div className="secondary-group">
                <div className="text">
                  { this.i18n(weekDaySpelling) }
                </div>
                <div>
                  <Basic.EnumSelectBox
                    className="weekday-select"
                    ref="weekDay"
                    enum={ WeekDayEnum }
                    value={ weekDay }
                    clearable={ false }
                    onChange={ this.onChangeWeekDay.bind(this) }/>
                </div>
                <div className="text">
                  { this.i18n('at') }
                </div>
                <div className="time-select">
                  <Basic.DateTimePicker
                    ref="weekTime"
                    mode="time"
                    value={ initTime }
                    onChange={ this.onChangeTime.bind(this) }
                    style={{ marginBottom: 0 }}
                  />
                </div>
              </div>
            )}

            {/* Month properties */}
            { intervalType === 'MONTH' && (
              <div className="secondary-group">
                <div className="text">
                  { this.i18n('monthly') }
                </div>
                <div>
                  <Basic.EnumSelectBox
                    ref="monthDay"
                    className="num-select"
                    options={ this._getDayInMonthOptions() }
                    value={ dayInMonth }
                    clearable={ false }
                    onChange={ this.onChangeDayInMonth.bind(this) } />
                </div>
                <div className="text">
                  { this.i18n('day') }
                  {' '}
                  { this.i18n('at') }
                </div>
                <Basic.Div className="time-select">
                  <Basic.DateTimePicker
                    ref="monthTime"
                    mode="time"
                    value={ initTime }
                    onChange={ this.onChangeTime.bind(this) }
                    style={{ marginBottom: 0 }}/>
                </Basic.Div>
              </div>
            )}
          </div>
        </Basic.LabelWrapper>

        {/* Warning for 29., 30., 31. days of month */}
        <Basic.Alert level="warning" icon="warning-sign" text={ this.i18n('warning') } rendered={ showMonthWarning }/>

        <div className="text-group cron-expression">
          { this.i18n('cronExpression') }
          { ' ' }
          { cronExpression }
        </div>

      </Basic.Div>
    );
  }
}
