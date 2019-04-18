import React from 'react';
import * as Basic from '../../basic';
import moment from 'moment';
//
import AbstractFormComponent from '../../basic/AbstractFormComponent/AbstractFormComponent';
import IntervalTypeEnum from '../../../enums/IntervalTypeEnum';
import WeekDayEnum from '../../../enums/WeekDayEnum';
import CronMinuteEnum from '../../../enums/CronMinuteEnum';
import CronHourEnum from '../../../enums/CronHourEnum';
import DayInMonthEnum from '../../../enums/DayInMonthEnum';
import Datetime from 'react-datetime';
import FilterTextField from '../Filter/FilterTextField';

class CronGenerator extends AbstractFormComponent {

  constructor(props) {
    super(props);
    this.state = {
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.MINUTE),
      weekDay: WeekDayEnum.findKeyBySymbol(WeekDayEnum.MONDAY),
      cronMinute: CronMinuteEnum.findKeyBySymbol(CronMinuteEnum.FIVE),
      cronHour: CronHourEnum.findKeyBySymbol(CronHourEnum.ONE),
      dayInMonth: DayInMonthEnum.findKeyBySymbol(DayInMonthEnum.ONE),
      time: '22:00',
      cronExpression: ''
    };
  }

  getComponentKey() {
    return 'entity.SchedulerTask.trigger.repeat';
  }

  onChangeIntervalType(intervalType) {
    this.setState({
      intervalType: intervalType.value
    }, () => this.generateCron());
  }

  onChangeWeekDay(weekDay) {
    this.setState({
      weekDay: weekDay.value
    }, () => this.generateCron());
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

  onChangeDayInMonth(dayInMonth) {
    this.setState({
      dayInMonth: dayInMonth.value
    }, () => this.generateCron());
  }

  onChangeTime(time) {
    const value = moment(time, 'HH:mm').format('HH:mm');
    this.setState({
      time: value
    }, () => this.generateCron());
  }

  generateCron() {
    this.setState({
      cronExpression: this.resolveIntervalToCron()
    });
  }

  resolveIntervalToCron() {
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
        // should never happened
    }
  }

  resolveMinutes() {
    // Minutes
    const time = this.state.time;
    const initMinute = time.split(':')[1];
    const repetitionRate = CronMinuteEnum.findSymbolByKey(this.state.cronMinute).description;

    // nahradit konstantou?
    let cronStartMinute = initMinute;
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
    // Hours
    const time = this.state.time;
    const initHour = time.split(':')[0];
    const initMinute = time.split(':')[1];
    const repetitionRate = CronHourEnum.findSymbolByKey(this.state.cronHour).description;

    // nahradit konstantou?
    let cronStartHour = initHour;
    if (initHour - repetitionRate > 0) {
      while (cronStartHour - repetitionRate > 0) {
        cronStartHour -= repetitionRate;
      }
    }

    const second = 0;
    const minute = initMinute;
    const hour = `${cronStartHour}/${repetitionRate}`;
    const monthDay = `*`;
    const monthInYear = `*`;
    const dayOfWeek = `?`;

    return `${second} ${minute} ${hour} ${monthDay} ${monthInYear} ${dayOfWeek}`;
  }

  resolveEveryDay() {
    // Day
    const time = this.state.time;
    const initHour = time.split(':')[0];
    const initMinute = time.split(':')[1];

    const second = 0;
    const minute = initMinute;
    const hour = initHour;
    const monthDay = '*';
    const monthInYear = '*';
    const dayOfWeek = '?';

    return `${second} ${minute} ${hour} ${monthDay} ${monthInYear} ${dayOfWeek}`;
  }

  resolveEveryWeek() {
    // Week
    const time = this.state.time;
    const initHour = time.split(':')[0];
    const initMinute = time.split(':')[1];
    const weekDay = WeekDayEnum.findSymbolByKey(this.state.weekDay).description;

    const second = 0;
    const minute = initMinute;
    const hour = initHour;
    const monthDay = '?';
    const monthInYear = '*';
    const dayOfWeek = weekDay;

    return `${second} ${minute} ${hour} ${monthDay} ${monthInYear} ${dayOfWeek}`;
  }

  resolveEveryMonth() {
    // Month
    const time = this.state.time;
    const initHour = time.split(':')[0];
    const initMinute = time.split(':')[1];
    const dayInMonth = DayInMonthEnum.findSymbolByKey(this.state.dayInMonth).description;

    const second = 0;
    const minute = initMinute;
    const hour = initHour;
    const monthDay = dayInMonth;
    const monthInYear = '*';
    const dayOfWeek = '?';

    return `${second} ${minute} ${hour} ${monthDay} ${monthInYear} ${dayOfWeek}`;
  }

  getBody() {
    const { showLoading, rendered } = this.props;
    const { intervalType, cronExpression, cronMinute, cronHour, weekDay, dayInMonth, time } = this.state;
    if (!rendered) {
      return null;
    }
    if (showLoading) {
      return (
        <Basic.Loading isStatic showLoading/>
      );
    }
    return (
      <Basic.AbstractForm
        showLoading={showLoading}
        style={{
          backgroundColor: 'palevioletred'
        }}
        className="">

        <Basic.Row
          style={{
            backgroundColor: 'lightblue',
            display: 'flex',
            alignItems: 'center'
          }}>
          <Basic.Col lg={ 4 } style={{ backgroundColor: '' }}>
            {/* <h3> */}
              {this.i18n('repeatEvery')}
            {/* </h3> */}
          </Basic.Col>
          <Basic.Col lg={ 2 }>
            <Basic.EnumSelectBox
              ref="cronMinute"
              label={ " " }
              enum={ CronMinuteEnum }
              value={ cronMinute }
              clearable={ false }
              hidden={ intervalType !== 'MINUTE' }
              onChange={ this.onChangeCronMinute.bind(this) }
              />
            <Basic.EnumSelectBox
              ref="cronHour"
              label={ " " }
              enum={ CronHourEnum }
              value={ cronHour }
              clearable={ false }
              hidden={ intervalType !== 'HOUR' }
              onChange={ this.onChangeCronHour.bind(this) }
              />
          </Basic.Col>
          <Basic.Col lg={ 3 }>
            <Basic.EnumSelectBox
              ref="intervalType"
              label={ " " }
              enum={ IntervalTypeEnum }
              value={ intervalType }
              clearable={ false }
              onChange={ this.onChangeIntervalType.bind(this) }
              input={ false }/>
          </Basic.Col>
        </Basic.Row>

        <Basic.Row
          hidden={ intervalType !== 'DAY' && intervalType !== 'HOUR' && intervalType !== 'MINUTE' }>
          <Basic.Col>
              { this.i18n('at') }
          </Basic.Col>
          <Basic.Col lg={ 4 }>
          <Datetime
            ref="dayTime"
            dateFormat={ false }
            timeFormat={ 'HH:mm' }
            value={ time }
            onChange={ this.onChangeTime.bind(this) }
          />
          </Basic.Col>
        </Basic.Row>

        {/* Week properties */}
        <Basic.Row
          hidden={ intervalType !== 'WEEK' }>
          <Basic.Col lg={ 2 }>
              { this.i18n('every') }
          </Basic.Col>
          <Basic.Col lg={ 2 }>
            <Basic.EnumSelectBox
              ref="weekDay"
              enum={ WeekDayEnum }
              value={ weekDay }
              clearable={ false }
              onChange={ this.onChangeWeekDay.bind(this) }/>
          </Basic.Col>
          <Basic.Col lg={ 1 }>
            { this.i18n('at') }
          </Basic.Col>
          <Basic.Col lg={ 4 }>
            <Datetime
              ref="weekTime"
              dateFormat={ false }
              timeFormat={ 'HH:mm' }
              value={ time }
              onChange={ this.onChangeTime.bind(this) }
            />
          </Basic.Col>
        </Basic.Row>

        {/* Month properties */}
        <Basic.Row
          hidden={ intervalType !== 'MONTH' }>
          <Basic.Col lg={ 2 }>
            <h3>
              { this.i18n('monthly') }
            </h3>
          </Basic.Col>
          <Basic.Col lg={ 2 }>
            <Basic.EnumSelectBox
              ref="monthDay"
              enum={ DayInMonthEnum }
              value={ dayInMonth }
              clearable={ false }
              onChange={ this.onChangeDayInMonth.bind(this) }
              />
          </Basic.Col>
          <Basic.Col lg={ 1 }>
            <h3>
              { this.i18n('day') }
            </h3>
          </Basic.Col>
          <Basic.Col lg={ 1 }>
            <h3>
              { this.i18n('at') }
            </h3>
          </Basic.Col>
          <Basic.Col lg={ 4 }>
            <Datetime
              ref="monthTime"
              dateFormat={ false }
              timeFormat={ 'HH:mm' }
              value={ time }
              onChange={ this.onChangeTime.bind(this) }
              />
          </Basic.Col>
        </Basic.Row>

        {/* Prepared for scheduled first start */}
        <Basic.Row rendered={ false }>
          <Basic.Col lg={ 2 }>
            <h3>
              { this.i18n('validFrom') }
            </h3>
          </Basic.Col>
          <Basic.Col lg={ 5 }>
            <Basic.DateTimePicker
              ref="fireTime"
              hidden={ intervalType !== 'MINUTE' && intervalType !== 'HOUR' && intervalType !== 'DAY' }
              />
            <Basic.DateTimePicker
              ref="fireTime"
              hidden={ intervalType !== 'WEEK' && intervalType !== 'MONTH' }
              mode="date"
              />
          </Basic.Col>
        </Basic.Row>

        <Basic.Row>
          <Basic.Col lg={ 6 }>
            <h3>
            { this.i18n('cronExpression') }
            { ' ' }
            { cronExpression }
            </h3>
          </Basic.Col>
        </Basic.Row>

      </Basic.AbstractForm>
    );
  }
}

export default CronGenerator;
