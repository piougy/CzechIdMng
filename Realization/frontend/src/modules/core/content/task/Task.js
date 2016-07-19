

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../../components/basic';
import { SecurityManager, IdentityManager, WorkflowTaskInstanceManager } from '../../../../modules/core/redux';
import * as Advanced from '../../../../components/advanced';
import DynamicTaskDetail from './DynamicTaskDetail';
import ComponentService from '../../../../services/ComponentService';

const identityManager = new IdentityManager();
const workflowTaskInstanceManager = new WorkflowTaskInstanceManager();
const componentService = new ComponentService();
let detailComponent;

class Task extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {showLoading: props.showLoading};
  }

  componentDidMount() {
    this.selectNavigationItem('tasks');
    const { taskID } = this.props.params;
    this.context.store.dispatch(workflowTaskInstanceManager.fetchEntityIfNeeded(taskID));
  }

  componentDidUpdate() {
    this.selectNavigationItem('tasks');
  }

  getContentKey() {
    return 'content.task';
  }

  render() {
    const { taskID } = this.props.params;
    const { readOnly, task} = this.props;
    // let DetailComponent;
    // if (task){
    //   DetailComponent = componentService.getComponent(task['_type']+'ApprovalTaskDetail');
    //   if (!DetailComponent){
    //       this.addMessage({title: this.i18n('message.task.detailNotFound'), level:'warning'});
    //       this.context.router.goBack();
    //       return null;
    //   }
    // }
    return (
        <div>
          {task ?
          <DynamicTaskDetail task={task} uiKey="dynamic-task-detail" taskManager={workflowTaskInstanceManager} readOnly={readOnly}/>
          :
          <Basic.Well showLoading={true}/>
          }
        </div>
    )
  }
}

Task.propTypes = {
  task: PropTypes.object,
  readOnly: PropTypes.bool
}
Task.defaultProps = {
  task: null,
  readOnly: false
}

function select(state, component) {
  const { taskID } = component.params;
  let task = workflowTaskInstanceManager.getEntity(state, taskID);

  return {
    task: task
  }
}

export default connect(select)(Task);
