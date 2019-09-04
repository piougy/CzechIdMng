import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import * as Domain from '../../domain';
import { WorkflowTaskInstanceManager } from '../../redux';
import TaskInstanceTable from './TaskInstanceTable';

/**
 * List of all instances tasks for administrators
 *
 * @autor Ondrej Kopr
 */
class TaskInstancesView extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.workflowTaskInstanceManager = new WorkflowTaskInstanceManager();
  }

  componentDidMount() {
    this.selectNavigationItems(['tasks', 'tasks-all']);
  }

  getContentKey() {
    return 'content.tasks.all';
  }

  getNavigationKey() {
    return 'task-instances';
  }

  render() {
    const searchParameters = new Domain.SearchParameters();
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <TaskInstanceTable
          uiKey="task-instance-table-all"
          taskInstanceManager={this.workflowTaskInstanceManager}
          filterOpened={false}
          showFilter
          showToolbar
          columns={['created', 'description', 'id', 'taskAssignee']}
          searchParameters={searchParameters} />
      </div>
    );
  }
}

TaskInstancesView.propTypes = {
};
TaskInstancesView.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(TaskInstancesView);
