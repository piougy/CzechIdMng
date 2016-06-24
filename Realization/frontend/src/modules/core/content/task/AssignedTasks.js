'use strict';

import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import { ApprovalTaskManager} from '../../../../redux';
import TaskStateEnum from '../../enums/TaskStateEnum';
import AssignedTaskTable from '../task/AssignedTaskTable';

/**
 * Users assigned tasks
 */
class AssignedTasks extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
    }
  }

  getContentKey() {
    return 'content.tasks-assigned';
  }

  componentDidMount(){
    this.selectNavigationItems(['tasks', 'tasks-assigned']);
  }

  render() {
    const { userID } = this.props.params;
    //
    return (
      <div>
        <Helmet title={this.i18n('navigation.menu.tasks')} />

        <Basic.Panel>
          <Basic.PanelHeader text={this.i18n('assigned')} />
          <AssignedTaskTable username={userID} showRowSelection={true}/>
        </Basic.Panel>
      </div>
    );
  }
}

AssignedTasks.propTypes = {
}
AssignedTasks.defaultProps = {
}

function select(state) {
  return {
  }
}

function select(state) {
  return {
  }
}

export default connect(select)(AssignedTasks)
