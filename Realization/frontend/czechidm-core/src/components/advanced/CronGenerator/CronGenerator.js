import React, { PropTypes } from 'react';
import * as Basic from '../../basic';
//
import AbstractFormComponent from '../../basic/AbstractFormComponent/AbstractFormComponent';
import IntervalTypeEnum from '../../../enums/IntervalTypeEnum';
import WeekDayEnum from '../../../enums/WeekDayEnum';
import CronMinuteEnum from '../../../enums/CronMinuteEnum';
import CronHourEnum from '../../../enums/CronHourEnum';
import DayInMonthEnum from '../../../enums/DayInMonthEnum';
// import Datetime from 'react-datetime';

class CronGenerator extends AbstractFormComponent {

  constructor(props) {
    super(props);
    this.state = {
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.HOUR),
      weekDay: WeekDayEnum.findKeyBySymbol(WeekDayEnum.MONDAY)
    };
  }

  getContentKey() {
    // return 'content.scheduler.schedule-tasks';
    // return 'content.scheduler.schedule-tasks.entity.SchedulerTask.trigger';
  }

  onChangeIntervalType(intervalType) {
    this.setState({
      intervalType: intervalType.value
    });
  }

  onChangeWeekDay(weekDay) {
    this.setState({
      weekDay: weekDay.value
    });
  }

  getBody() {
    const { showLoading, rendered } = this.props;
    const { intervalType } = this.state;
    if (!rendered) {
      return null;
    }
    if (showLoading) {
      return (
        <Basic.Loading isStatic showLoading/>
      );
    }
    return (
      <Basic.AbstractForm showLoading={showLoading}>

        <Basic.Row>
          <Basic.Col lg={ 6 }>
            <div>
              { this.i18n('entity.SchedulerTask.trigger.repeat.label') }
            </div>
          </Basic.Col>
          <Basic.Col lg={ 3 }>
            <Basic.EnumSelectBox
              ref="cronMinute"
              enum={ CronMinuteEnum }
              hidden={ intervalType !== 'MINUTE'}
              />
            <Basic.EnumSelectBox
              ref="cronHour"
              enum={ CronHourEnum }
              hidden={ intervalType !== 'HOUR'}
              />
          </Basic.Col>
          <Basic.Col lg={ 3 }>
            <Basic.EnumSelectBox
              ref="intervalType"
              enum={ IntervalTypeEnum }
              // required
              onChange={this.onChangeIntervalType.bind(this)}/>
          </Basic.Col>
        </Basic.Row>

        {/* Week properties */}
        <Basic.Row
          hidden={ intervalType !== 'WEEK' }>
          <Basic.Col lg={ 3 }>
            <div>
              { 'Každé' }
            </div>
          </Basic.Col>
          <Basic.Col lg={ 3 }>
            <Basic.EnumSelectBox
              ref="weekDay"
              enum={ WeekDayEnum }
              // required
              onChange={this.onChangeWeekDay.bind(this)}/>
          </Basic.Col>
          <Basic.Col lg={ 1 }>
            <div>
              { 'v' }
            </div>
          </Basic.Col>
          <Basic.Col lg={ 5 }>
            <Basic.DateTimePicker
              ref="fireTime"
              mode="time"
              // required
            />
          </Basic.Col>
        </Basic.Row>

        {/* Month properties */}
        <Basic.Row
          hidden={ intervalType !== 'MONTH' }>
          <Basic.Col lg={ 2 }>
            <div>
              { 'Měsíčně' }
            </div>
          </Basic.Col>
          <Basic.Col lg={ 3 }>
            <Basic.EnumSelectBox
              ref="monthDay"
              enum={ DayInMonthEnum }/>
          </Basic.Col>
          <Basic.Col lg={ 2 }>
            <div>
              { 'den v' }
            </div>
          </Basic.Col>
          <Basic.Col lg={ 5 }>
            <Basic.DateTimePicker
              ref="fireTime"
              mode="time"
              // required
            />
          </Basic.Col>
        </Basic.Row>

        <Basic.Row>
          <Basic.Col lg={ 2 }>
            <div>
              { this.i18n('entity.SchedulerTask.trigger.repeat.firstRun') }
            </div>
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

      </Basic.AbstractForm>
    );
  }
}

export default CronGenerator;
