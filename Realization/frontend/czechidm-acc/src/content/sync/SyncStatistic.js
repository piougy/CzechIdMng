import React from 'react';
import moment from 'moment';
//
import { Basic, Services } from 'czechidm-core';

/**
 * Statistic for given synchronization log
 *
 * @author Vít Švanda
 */
class SyncStatistic extends Basic.AbstractContent {

  render() {
    const {log} = this.props;

    const actions = [];
    const started = log.started;
    const ended = log.ended ? log.ended : moment().utc().valueOf();
    const timeDiff = moment.utc(moment.duration(moment(ended).diff(moment(started))).asMilliseconds());
    const timeDiffHumanized = moment
      .duration(moment(ended)
        .diff(moment(started)))
      .locale(Services.LocalizationService.getCurrentLanguage())
      .humanize();
    let allOperationsCount = 0;
    for (const action of log.syncActionLogs) {
      allOperationsCount += action.operationCount;
    }
    const itemsPerSec = Math.round((allOperationsCount / timeDiff * 1000) * 100) / 100;
    if (log.running || log.ended) {
      actions.push(
        <div>
          <Basic.Label
            style={{marginRight: '5px'}}
            level="info"
            title={timeDiffHumanized}
            text={timeDiff.format(this.i18n('format.times'))}/>
          <strong>
            {this.i18n(`acc:entity.SynchronizationLog.statistic.timeDiff`)}
          </strong>
        </div>
      );
    }
    actions.push(
      <div>
        <Basic.Label style={{marginRight: '5px'}} level="info" text={allOperationsCount}/>
        <strong>
          {this.i18n(`acc:entity.SynchronizationLog.statistic.allOperations`)}
        </strong>
      </div>
    );
    if (log.running || log.ended) {
      actions.push(
        <div>
          <Basic.Label style={{marginRight: '5px'}} level="info" text={itemsPerSec}/>
          <strong>
            {this.i18n(`acc:entity.SynchronizationLog.statistic.itemsPerSec`)}
          </strong>
        </div>
      );
    }
    return (
      <div>
        {actions}
      </div>
    );
  }

}

export default SyncStatistic;
