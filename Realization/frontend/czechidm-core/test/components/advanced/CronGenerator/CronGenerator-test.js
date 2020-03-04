import React from 'react';
import TestUtils from 'react-dom/test-utils';
import moment from 'moment';
import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
//
import * as Advanced from '../../../../src/components/advanced';
import IntervalTypeEnum from '../../../../src/enums/IntervalTypeEnum';
import WeekDayEnum from '../../../../src/enums/WeekDayEnum';
import CronMinuteEnum from '../../../../src/enums/CronMinuteEnum';
import CronHourEnum from '../../../../src/enums/CronHourEnum';
//
chai.use(dirtyChai);

/**
 * CronGenerator method tests
 *
 * @author Petr Hanák
 */

describe('CronGenerator', () => {
  const cronGeneratorInstance = TestUtils.renderIntoDocument(<Advanced.CronGenerator/>);
  it('- resolve minutes interval to cron', () => {
    // every 15 minutes from 18:37
    cronGeneratorInstance.setState({
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.MINUTE),
      cronMinute: CronMinuteEnum.findKeyBySymbol(CronMinuteEnum.FIFTEEN),
      time: moment().set({ hour: 18, minute: 37 }),
      executeDate: moment().set({ hour: 18, minute: 37 })
    });
    const cronExpression = cronGeneratorInstance.getCron();
    const cronCorrectExpression = '0 7/15 * * * ?';

    expect(cronExpression).to.equal(cronCorrectExpression);
  });
  it('- resolve hours interval to cron', () => {
    // every 8 hours from 10:53
    cronGeneratorInstance.setState({
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.HOUR),
      cronHour: CronHourEnum.findKeyBySymbol(CronHourEnum.EIGHT),
      time: moment().set({ hour: 10, minute: 53}),
      executeDate: moment().set({ hour: 10, minute: 53})
    });
    const cronExpression = cronGeneratorInstance.getCron();
    const cronCorrectExpression = '0 53 2/8 * * ?';

    expect(cronExpression).to.equal(cronCorrectExpression);
    // test of cron expression in state
    cronGeneratorInstance.generateCron();
    expect(cronGeneratorInstance.state.cronExpression).to.equal(cronCorrectExpression);
  });
  it('- resolve every day interval to cron', () => {
    // every day at 14:32
    cronGeneratorInstance.setState({
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.DAY),
      time: moment().set({ hour: 14, minute: 32 }),
      executeDate: moment().set({ hour: 14, minute: 32 })
    });
    const cronExpression = cronGeneratorInstance.getCron();
    const cronCorrectExpression = '0 32 14 * * ?';

    expect(cronExpression).to.equal(cronCorrectExpression);
    // test of cron expression in state
    cronGeneratorInstance.generateCron();
    expect(cronGeneratorInstance.state.cronExpression).to.equal(cronCorrectExpression);
  });
  it('- resolve every week interval to cron sat', () => {
    // every saturday at 02:01
    cronGeneratorInstance.setState({
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.WEEK),
      weekDay: WeekDayEnum.findKeyBySymbol(WeekDayEnum.SATURDAY),
      time: moment().set({ hour: 2, minute: 1 }),
      executeDate: moment().set({ hour: 2, minute: 1 })
    });
    const cronExpression = cronGeneratorInstance.getCron();
    const cronCorrectExpression = '0 1 2 ? * sat';

    expect(cronExpression).to.equal(cronCorrectExpression);
    // test of cron expression in state
    cronGeneratorInstance.generateCron();
    expect(cronGeneratorInstance.state.cronExpression).to.equal(cronCorrectExpression);
  });
  it('- resolve every week interval to cron', () => {
    // every saturday at 23:20
    cronGeneratorInstance.setState({
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.MONTH),
      dayInMonth: 28,
      time: moment().set({ hour: 23, minute: 20 }),
      executeDate: moment().set({ hour: 23, minute: 20 })
    });
    const cronExpression = cronGeneratorInstance.getCron();
    const cronCorrectExpression = '0 20 23 28 * ?';

    expect(cronExpression).to.equal(cronCorrectExpression);
    // test of cron expression in state
    cronGeneratorInstance.generateCron();
    expect(cronGeneratorInstance.state.cronExpression).to.equal(cronCorrectExpression);
  });
});
