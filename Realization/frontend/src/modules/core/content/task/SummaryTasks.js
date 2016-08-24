import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import { AbstractContent, Panel, PanelHeader } from '../../../../components/basic';
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import { ApprovalTaskManager} from '../../../../redux';
import TaskStateEnum from '../../enums/TaskStateEnum';


class SummaryTasks extends AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.approvalTaskManager = new ApprovalTaskManager();
    const { userID } = this.props.params;

    this.state = {
      open: true,
      forceSearchParameters: {
        filter: {
          filters: [
            {
              field: 'relatedUsers.identityName',
              value: userID,
              relation: 'EQ'
            }
          ]
        }
      }
    };
  }

  getContentKey() {
    return 'content.tasks-summary';
  }


  componentDidMount() {
    this.selectNavigationItems(['tasks', 'tasks-summary']);
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  render() {
    const { forceSearchParameters } = this.state;

    return (
      <div>
        <Helmet title={this.i18n('navigation.menu.tasks')} />
        <Panel>
          <PanelHeader text={this.i18n('summary')} />
              <Advanced.Table
                ref="table"
                uiKey="summary_approval_tasks_table"
                manager={this.approvalTaskManager}
                filter={this._getFilterDefinition()}
                  filterOpened>
                <Advanced.Column property="taskState" width="5%" sort face="text"
                   cell={<Basic.EnumCell enumClass={TaskStateEnum}/>}/>
                <Advanced.ColumnLink to="task/:taskId" property="taskName" width="30%" sort face="text"/>
                <Advanced.Column property="createdDate" width="5%" sort face="date" />
                <Advanced.Column property="closedDate" width="5%" sort face="date" />
                <Advanced.ColumnLink to="user/:approvedBy/profile" property="approvedBy" width="5%" sort face="text" />
                <Advanced.Column property="relatedTaskId" width="5%" sort face="text" />
                <Advanced.Column property="taskId" width="5%" sort face="text" />
              </Advanced.Table>
        </Panel>
      </div>
    );
  }

/**
 * Filter for table
 * @return filter definiton
 */
  _getFilterDefinition() {
    return (
        <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
          <Basic.AbstractForm ref="filterForm" className="form-horizontal">
            <Basic.Row>
              <div className="col-lg-4">
                <Advanced.Filter.DateTimePicker
                  mode="datetime"
                  ref="filterCreatedDateFrom"
                  field="createdDate"
                  relation="GE"
                  label={this.i18n('filter.createdDateFrom.label')}/>
              </div>
              <div className="col-lg-4">
                <Advanced.Filter.DateTimePicker
                  mode="datetime"
                  ref="filterCreatedDateTill"
                  field="createdDate"
                  relation="LE"
                  label={this.i18n('filter.createdDateTill.label')}/>
              </div>
              <div className="col-lg-4 text-right">
                <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
              </div>
            </Basic.Row>
            <Basic.Row>
              <div className="col-lg-4">
                <Advanced.Filter.DateTimePicker
                  mode="datetime"
                  ref="filterCloseDateFrom"
                  field="closedDate"
                  relation="GE"
                  label={this.i18n('filter.closeDateFrom.label')}/>
              </div>
              <div className="col-lg-4">
                <Advanced.Filter.DateTimePicker
                  mode="datetime"
                  ref="filterClosedDateDateTill"
                  field="closedDate"
                  relation="LE"
                  label={this.i18n('filter.closeDateTill.label')}/>
              </div>
              <div className="col-lg-4">
              </div>
            </Basic.Row>
            <Basic.Row>
              <div className="col-lg-4">
                <Advanced.Filter.TextField
                  ref="approvedBy"
                  placeholder={this.i18n('filter.approvedBy.placeholder')}
                  label={this.i18n('filter.approvedBy.label')}/>
              </div>
              <div className="col-lg-4">
                <Advanced.Filter.EnumSelectBox
                  ref="taskState"
                  label={this.i18n('filter.taskState.label')}
                  placeholder={this.i18n('filter.taskState.placeholder')}
                  multiSelect={true}
                  enum={TaskStateEnum}/>
              </div>
              <div className="col-lg-4">
                <Advanced.Filter.TextField
                  ref="taskId"
                  relation="EQ"
                  placeholder={this.i18n('filter.taskId.placeholder')}
                  label={this.i18n('filter.taskId.label')}/>
              </div>
            </Basic.Row>
            <Basic.Row className="last">
              <div className="col-lg-4">
                <Advanced.Filter.TextField
                  ref="taskName"
                  placeholder={this.i18n('filter.taskName.placeholder')}
                  label={this.i18n('filter.taskName.label')}/>
              </div>
              <div className="col-lg-4">
                <Advanced.Filter.TextField
                  ref="note"
                  placeholder={this.i18n('filter.note.placeholder')}
                  label={this.i18n('filter.note.label')}/>
              </div>
            </Basic.Row>
          </Basic.AbstractForm>
        </Advanced.Filter>
      );
  }
}

SummaryTasks.propTypes = {
};
SummaryTasks.defaultProps = {
};

function select(state) {
  if (!state.data.ui.my_approval_tasks_table) {
    return {};
  }
  return {
    _searchParameters: state.data.ui.my_approval_tasks_table.searchParameters
  };
}

export default connect(select)(SummaryTasks);
