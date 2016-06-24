'use strict';

import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../../../components/basic';
import ConnectedAssignedTaskTable, { AssignedTaskTable } from '../task/AssignedTaskTable';

class AssignedTaskDashboard extends Basic.AbstractContent {

  render() {
    const { userContext } = this.props;

    return (
      <Basic.Panel rendered={false}>
        <Basic.PanelHeader text="Přiřazené úkoly"/>
        <ConnectedAssignedTaskTable
          username={userContext.username}
          columns={AssignedTaskTable.defaultProps.columns.filter(property => { return property !== 'relatedTaskId'})}/>
      </Basic.Panel>
    );
  }
}

AssignedTaskDashboard.propTypes = {
  userContext: React.PropTypes.object
};

AssignedTaskDashboard.defaultProps = {
  userContext: null
};

function select(state) {
  return {
    userContext: state.security.userContext
  }
}

export default connect(select)(AssignedTaskDashboard);
