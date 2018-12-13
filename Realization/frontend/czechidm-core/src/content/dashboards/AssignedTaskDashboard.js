import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { WorkflowTaskInstanceManager, SecurityManager } from '../../redux';
import TaskInstanceTable from '../task/TaskInstanceTable';

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
    const { identity } = this.props;

    if (!identity || !SecurityManager.hasAuthority('WORKFLOWTASK_READ') ) {
      return null;
    }
    //
    return (
      <div>
        <Basic.ContentHeader
          icon="tasks"
          text={ this.i18n('content.tasks-assigned.assigned') }/>
        <Basic.Panel>
          <TaskInstanceTable
            uiKey={ `task-instance-dashboard-${ identity.username }-table` }
            username={ identity.username }
            taskInstanceManager={ this.workflowTaskInstanceManager }
            filterOpened={ false }/>
        </Basic.Panel>
      </div>
    );
  }
}

function select(state) {
  return {
    userContext: state.security.userContext
  };
}

export default connect(select)(AssignedTaskDashboard);
