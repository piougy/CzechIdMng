import React from 'react';
import * as Basic from '../../components/basic';
import { SecurityManager } from '../../redux';
import RunningTasks from '../scheduler/RunningTasks';
/**
 * Identity info with link to profile
 *
 * @author Radek Tomiška
 */
export default class LongRunningTaskDashboard extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'dashboard.longRunningTaskDashboard';
  }

  render() {
    const { identity } = this.props;
    //
    if (!SecurityManager.hasAnyAuthority(['SCHEDULER_READ'])) {
      return null;
    }
    //
    return (
      <div>
        <Basic.ContentHeader
          icon="fa:calendar-times-o"
          text={ this.i18n('dashboard.longRunningTaskDashboard.header') }/>
        <Basic.Panel>
          <RunningTasks creatorId={ identity ? identity.id : null } />
        </Basic.Panel>
      </div>
    );
  }
}
