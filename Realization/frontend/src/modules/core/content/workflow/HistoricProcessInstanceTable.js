'use strict';

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import Immutable from 'immutable';
import uuid from 'uuid';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';

/**
* Table of historic processes
*/
export class HistoricProcessInstanceTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
      detail: {
        show: false,
        entity: {}
      }
    }
  }

  getContentKey() {
    return 'content.workflow.history.process';
  }

  componentDidMount() {
  }

  componentDidUpdate() {
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilter(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  showDetail(entity) {
    this.setState({
      detail: {
        show: true,
        showLoading: false,
        entity: entity
      }
    }, () => {
      this.refs.form.setData(entity);
      this.refs.name.focus();
    });
  }

  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: {}
      }
    });
  }

  render() {
    const { uiKey, workflowHistoricProcessInstanceManager, columns, _showLoading } = this.props;
    const { filterOpened, detail } = this.state;

    return (
      <div>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={workflowHistoricProcessInstanceManager}
          showRowSelection={false}
          filterOpened={filterOpened}>

          <Advanced.Column
            header=""
            property=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex])}/>
                );
              }
            }
            sort={false}/>
          <Advanced.ColumnLink property="name" to="workflow/history/processes/:id" sort={false} face="text" rendered={_.includes(columns, 'name')}/>
            <Advanced.Column
              property="processVariables.APPLICANT_USERNAME"
              header={this.i18n('entity.WorkflowHistoricProcessInstance.applicant')}
              sort={false}
              face="text"/>
          <Advanced.Column property="startTime" sort={true} face="datetime" rendered={_.includes(columns, 'startTime')}/>
          <Advanced.Column property="endTime" sort={true} face="datetime" rendered={_.includes(columns, 'endTime')}/>
          <Advanced.Column property="startActivityId" sort={false} face="text" rendered={_.includes(columns, 'startActivityId')}/>
          <Advanced.Column property="deleteReason" sort={false} face="text" rendered={_.includes(columns, 'deleteReason')}/>
          <Advanced.Column property="superProcessInstanceId" sort={false} face="text" rendered={_.includes(columns, 'superProcessInstanceId')}/>
          <Advanced.Column property="id" sort={false} face="text" rendered={_.includes(columns, 'id')}/>
        </Advanced.Table>
      </div>
    );
  }
}

HistoricProcessInstanceTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  workflowHistoricProcessInstanceManager: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool
};

HistoricProcessInstanceTable.defaultProps = {
  columns: ['deleteReason','id', 'endTime', 'startTime', 'name'],
  filterOpened: false,
  _showLoading: false
};

function select(state, component) {
  return {
    _searchParameters: state.data.ui[component.uiKey] ? state.data.ui[component.uiKey].searchParameters : {},
    _showLoading: component.workflowHistoricProcessInstanceManager.isShowLoading(state, `${component.uiKey}-detail`)
  };
}

export default connect(select, null, null, { withRef: true })(HistoricProcessInstanceTable);
