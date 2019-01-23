import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { SecurityManager, LongRunningTaskManager } from '../../redux';
import RunningTasks from '../scheduler/RunningTasks';

const manager = new LongRunningTaskManager();
const uiKeyPrefix = 'long-running-taks-table-';

/**
 * Identity info with link to profile
 *
 * @author Radek Tomiška
 */
class LongRunningTaskDashboard extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'dashboard.longRunningTaskDashboard';
  }

  render() {
    const { identity, _total } = this.props;
    //
    if (!SecurityManager.hasAnyAuthority(['SCHEDULER_READ'])) {
      return null;
    }
    //
    return (
      <div className={ _total ? '' : 'hidden' }>
        <Basic.ContentHeader
          icon="fa:calendar-times-o"
          text={ this.i18n('dashboard.longRunningTaskDashboard.header') }/>
        <Basic.Panel>
          <RunningTasks
            manager={ manager }
            uiKey={ `${ uiKeyPrefix }${ identity ? identity.id : 'dashboard' }` }
            creatorId={ identity ? identity.id : null } />
        </Basic.Panel>
      </div>
    );
  }
}

function select(state, component) {
  const uiKey = `${ uiKeyPrefix }${ component.identity ? component.identity.id : 'dashboard' }`;
  const ui = state.data.ui[uiKey];
  if (!ui) {
    return {};
  }
  return {
    _total: ui.total
  };
}

export default connect(select)(LongRunningTaskDashboard);
