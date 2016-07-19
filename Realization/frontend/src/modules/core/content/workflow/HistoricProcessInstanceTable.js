

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import Immutable from 'immutable';
import uuid from 'uuid';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import {WorkflowProcessDefinitionManager} from '../../redux';


const workflowProcessDefinitionManager = new WorkflowProcessDefinitionManager();
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
    this.context.router.push('workflow/history/processes/'+entity.id)
  }

  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: {}
      }
    });
  }

  _filter() {
   return(  <Advanced.Filter onSubmit={this._useFilter.bind(this)}>
      <Basic.AbstractForm ref="filterForm">
        <Basic.Row className="last">
          <div className="col-lg-4">
            <Advanced.Filter.TextField
              ref="name"
              placeholder={this.i18n('entity.Organization.name')}
              value=""
              label={this.i18n('entity.Organization.name')}/>
          </div>
          <div className="col-lg-5">
            <Advanced.Filter.SelectBox
              ref="processDefinition"
              label={this.i18n('filter.processDefinition.label')}
              placeholder={this.i18n('filter.processDefinition.placeholder')}
              multiSelect={false}
              manager={workflowProcessDefinitionManager}/>
          </div>
          <div className="col-lg-3 text-right">
            <Advanced.Filter.FilterButtons cancelFilter={this._cancelFilter.bind(this)}/>
          </div>
        </Basic.Row>
      </Basic.AbstractForm>
    </Advanced.Filter>)
  }

  _useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilter(this.refs.filterForm);
  }

  _cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  render() {
    const { uiKey, workflowHistoricProcessInstanceManager, columns, _showLoading, forceSearchParameters } = this.props;
    const { filterOpened, detail } = this.state;

    return (
      <div>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          forceSearchParameters={forceSearchParameters}
          manager={workflowHistoricProcessInstanceManager}
          showRowSelection={false}
          filter={this._filter()}
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
              property="processVariables.applicantUsername"
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
  filterOpened: PropTypes.bool,
  forceSearchParameters: PropTypes.object
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
