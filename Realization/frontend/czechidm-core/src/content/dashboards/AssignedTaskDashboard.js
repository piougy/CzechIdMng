import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { WorkflowTaskInstanceManager, SecurityManager } from '../../redux';
import TaskInstanceTable from '../task/TaskInstanceTable';

const uiKeyPrefix = 'task-instance-dashboard-';

/**
 * Assigned workflow tasks
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 */
class AssignedTaskDashboard extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.workflowTaskInstanceManager = new WorkflowTaskInstanceManager();
  }

  render() {
    const { identity, userContext, _total } = this.props;

    if (!identity || !SecurityManager.hasAuthority('WORKFLOWTASK_READ') ) {
      return null;
    }
    if (identity.username !== userContext.username && !SecurityManager.hasAuthority('WORKFLOWTASK_ADMIN')) {
      return null;
    }
    //
    return (
      <div className={ _total ? '' : 'hidden' }>
        <Basic.ContentHeader
          icon="tasks"
          text={ this.i18n('content.tasks-assigned.assigned') }/>
        <Basic.Panel>
          <TaskInstanceTable
            uiKey={ `${ uiKeyPrefix }${ identity.username }` }
            username={ identity.username }
            taskInstanceManager={ this.workflowTaskInstanceManager }
            filterOpened={ false }/>
        </Basic.Panel>
      </div>
    );
  }
}

function select(state, component) {
  const uiKey = `${ uiKeyPrefix }${ component.identity ? component.identity.username : '' }`;
  const ui = state.data.ui[uiKey];
  return {
    userContext: state.security.userContext,
    _total: ui ? ui.total : null
  };
}

export default connect(select)(AssignedTaskDashboard);
