'use strict';

import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import { ApprovalTaskManager} from '../../../../redux';
import TaskStateEnum from '../../enums/TaskStateEnum';

/**
 * Assiged tasts for given user
 */
export class AssignedTaskTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      open: true,
      forceSearchParameters: {
        filter: {
          filters: [
            {
              field: 'taskState',
              value: 'PENDING',
              relation: 'EQ'
            },
            {
              field: 'approvers.identityName',
              value: this.props.username,
              relation: 'EQ'
            }
          ]
        }
      },
      bulkLoading: false,
      bulkCounter: 0,
      bulkCount: 0,
      bulkAction: null
    };
    this.approvalTaskManager = new ApprovalTaskManager();
  }

  componentDidMount() {
  }

  componentDidUpdate() {
  }

  onResolveTask(approved, bulkActionValue, selectedRows) {
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`content.tasks-assigned.action.${bulkActionValue}.message`, { count: selectedRows.length }),
      this.i18n(`content.tasks-assigned.action.${bulkActionValue}.header`, { count: selectedRows.length})
    ).then(result => {
      // TODO: long runnig task on server
      this.setState({
        bulkAction: bulkActionValue,
        bulkLoading: true,
        bulkCounter: 0,
        bulkCount: selectedRows.length
      }, () => {
        this._resolveTask(approved, selectedRows, 0);
      });
    }, (err) => {
      // nothing
    });
  }

  _resolveTask(approved, taskIds, index) {
    const bulkActionValue = approved ? 'approve' : 'disapprove';
    //
    let promise;
    if (approved){
      promise = this.approvalTaskManager.getService().approve(taskIds[index], null);
    } else {
      promise = this.approvalTaskManager.getService().disapprove(taskIds[index], null);
    }
    return promise.then(response => {
      return response.json();
    })
    .then(json => {
      if (!json.error) {
        const { bulkCounter } = this.state;
        this.setState({
          bulkCounter: bulkCounter + 1
        }, () => {
          // next
          if (taskIds.length > index + 1) {
            this._resolveTask(approved, taskIds, index + 1);
          } else {
            this.addMessage({ message: this.i18n(`content.tasks-assigned.action.${bulkActionValue}.success`, { count: taskIds.length }) });
            this.stopBulkAction();
          }
        });
      } else {
        this.stopBulkAction();
        this.addErrorMessage({ title: this.i18n(`content.tasks-assigned.action.${bulkActionValue}.error`, { task: taskIds[index] }) }, json.error);
      }
      return json;
    }).catch(error => {
      this.stopBulkAction();
      this.addErrorMessage({ title: this.i18n(`content.tasks-assigned.action.${bulkActionValue}.error`, { task: taskIds[index] }) }, error);
    });
  }

  stopBulkAction() {
    this.setState({
      bulkLoading: false,
      bulkCounter: 0,
    });
    this.refs.table.getWrappedInstance().reload();
  }

  render() {
    const { username, showRowSelection, columns } = this.props;
    const { forceSearchParameters, bulkAction, bulkCounter, bulkCount, bulkLoading } = this.state;
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-disapprove" level="danger"/>
        <Basic.Confirm ref="confirm-approve"/>

        <Advanced.ModalProgressBar
          show={bulkLoading}
          text={this.i18n(`content.tasks-assigned.action.${bulkAction}.header`, { count: bulkCount})}
          count={bulkCount}
          counter={bulkCounter}/>

        <Advanced.Table
          ref="table"
          uiKey={`${username}-approval-tasks-table`}
          manager={this.approvalTaskManager}
          forceSearchParameters={forceSearchParameters}
          showRowSelection={showRowSelection}
          actions={
            [
              { value: 'approve', niceLabel: this.i18n('content.tasks-assigned.action.approve.action'), action: this.onResolveTask.bind(this, true) },
              { value: 'disapprove', niceLabel: this.i18n('content.tasks-assigned.action.disapprove.action'), action: this.onResolveTask.bind(this, false) },
            ]
          }
          >
          <Advanced.ColumnLink to="task/:taskId" property="taskName" sort={true} face="text" rendered={_.includes(columns, 'taskName')}/>
          <Advanced.Column property="createdDate" width="15%" sort={true} face="date" rendered={_.includes(columns, 'createdDate')}/>
          <Advanced.Column property="relatedTaskId" width="15%" sort={true} face="text" rendered={_.includes(columns, 'relatedTaskId')}/>
        </Advanced.Table>
      </div>
    );
  }
}

AssignedTaskTable.propTypes = {
  /**
   * Assigned tasks will be shown for given username
   */
  username: PropTypes.string.isRequired,
  /**
   * If bulk actions is available
   */
  showRowSelection: PropTypes.bool,
  columns: PropTypes.arrayOf(PropTypes.string)
};
AssignedTaskTable.defaultProps = {
  showRowSelection: false,
  columns: ['taskName', 'createdDate', 'relatedTaskId']
};

function select(state, component) {
  return {
  };
}

export default connect(select, null, null, { withRef: true })(AssignedTaskTable);
