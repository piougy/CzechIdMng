import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { WorkflowTaskInstanceManager, SecurityManager } from '../../redux';
import TaskInstanceTable from '../task/TaskInstanceTable';

class AssignedTaskDashboard extends Basic.AbstractContent {
  constructor(props, context) {
    super(props, context);
    this.workflowTaskInstanceManager = new WorkflowTaskInstanceManager();
  }

  render() {
    return (
      <Basic.Panel rendered={SecurityManager.hasAuthority('WORKFLOWTASK_READ')}>
        <Basic.PanelHeader text={this.i18n('content.tasks-assigned.assigned')}/>
        <TaskInstanceTable uiKey="task_instance_dashboard_table" taskInstanceManager={this.workflowTaskInstanceManager} filterOpened={false}/>
      </Basic.Panel>
    );
  }
}

function select(state) {
  return {
    userContext: state.security.userContext
  };
}

export default connect(select)(AssignedTaskDashboard);
