import React from 'react';
import * as Basic from '../../components/basic';
import { SecurityManager } from '../../redux';
import { AuthenticateService } from '../../services';
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
    const userContext = AuthenticateService.getUserContext();
    if (!SecurityManager.hasAnyAuthority(['SCHEDULER_READ'])) {
      return null;
    }
    return (
      <Basic.Panel>
        <Basic.PanelHeader text={this.i18n('header')} />
        <RunningTasks creatorId={userContext.id} />
      </Basic.Panel>
    );
  }
}
