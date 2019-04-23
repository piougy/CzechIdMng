// import chai, { expect } from 'chai';
// import dirtyChai from 'dirty-chai';
// chai.use(dirtyChai);

// import {CronGenerator} from '../../../../src/components/advanced/CronGenerator';
// import IntervalTypeEnum from '../../../../src/enums/IntervalTypeEnum';
// import WeekDayEnum from '../../../../src/enums/WeekDayEnum';
// import CronMinuteEnum from '../../../../src/enums/CronMinuteEnum';
// import CronHourEnum from '../../../../src/enums/CronHourEnum';
// import DayInMonthEnum from '../../../../src/enums/DayInMonthEnum';

// /**
//  * CronGenerator method tests
//  *
//  * @author Petr Hanák
//  */

// describe('CronGenerator', function testCronGenerator() {
//   it('- resolve minutes interval to cron', function validate() {
//     const component = new CronGenerator();
//     // every 15 minutes from 18:37
//     component.setState({
//       intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.MINUTE),
//       cronMinute: CronMinuteEnum.findKeyBySymbol(CronMinuteEnum.FIFTEEN),
//       time: '18:37',
//     });
//     const cronOne = '0 7/15 * * * ?';
//     const cronTwo = component.resolveIntervalToCron();

//     expect(cronOne.equals(cronTwo)).to.be.true();
//   });
// });
