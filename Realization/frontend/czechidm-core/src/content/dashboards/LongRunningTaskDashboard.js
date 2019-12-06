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
      <Basic.Div className={ _total ? '' : 'hidden' }>
        <Basic.ContentHeader
          icon="component:scheduled-task"
          text={ this.i18n('dashboard.longRunningTaskDashboard.header') }/>
        <Basic.Panel>
          <RunningTasks
            manager={ manager }
            uiKey={ `${ uiKeyPrefix }${ identity ? identity.id : 'dashboard' }` }
            creatorId={ identity ? identity.id : null } />
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

function select(state, component) {
  const uiKey = `${ uiKeyPrefix }${ component.identity ? component.identity.id : 'dashboard' }`;
  const ui = state.data.ui[uiKey];
  if (!ui) {
    return {
      i18nReady: state.config.get('i18nReady')
    };
  }
  return {
    _total: ui.total,
    i18nReady: state.config.get('i18nReady')
  };
}

export default connect(select)(LongRunningTaskDashboard);
