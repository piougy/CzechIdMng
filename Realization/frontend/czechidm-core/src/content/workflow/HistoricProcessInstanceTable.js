import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { WorkflowProcessDefinitionManager } from '../../redux';


const workflowProcessDefinitionManager = new WorkflowProcessDefinitionManager();
/**
* Table of historic processes
*
* @author Vít Švanda
*/
export class HistoricProcessInstanceTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
      detail: {
        show: false,
        entity: {}
      }
    };
  }

  getContentKey() {
    return 'content.workflow.history.process';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this._initComponent(this.props);
  }

  /**
   * In component will recive props compare forceSearchParameters. If are different
   * call again _initComponent with nextProps.
   */
  componentWillReceiveProps(nextProps) {
    const { forceSearchParameters } = nextProps;
    if (forceSearchParameters && forceSearchParameters !== this.props.forceSearchParameters) {
      this._initComponent(nextProps);
    }
  }

  _initComponent() {
    this.refs.table.getWrappedInstance().reload();
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

  showDetail(entity) {
    this.context.router.push('workflow/history/processes/' + entity.id);
  }

  _filter() {
    return (<Advanced.Filter onSubmit={this._useFilter.bind(this)}>
      <Basic.AbstractForm ref="filterForm">
        <Basic.Row className="last">
          <Basic.Col lg={ 4 }>
            <Advanced.Filter.TextField
              ref="name"
              placeholder={this.i18n('name')}/>
          </Basic.Col>
          <Basic.Col lg={ 5 }>
            <Advanced.Filter.SelectBox
              ref="processDefinitionKey"
              placeholder={this.i18n('filter.processDefinition.placeholder')}
              multiSelect={false}
              manager={workflowProcessDefinitionManager}/>
          </Basic.Col>
          <Basic.Col lg={ 3 } className="text-right">
            <Advanced.Filter.FilterButtons cancelFilter={this._cancelFilter.bind(this)}/>
          </Basic.Col>
        </Basic.Row>
      </Basic.AbstractForm>
    </Advanced.Filter>);
  }

  _useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  _cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  _getWfProcessCell({ rowIndex, data}) {
    const entity = data[rowIndex];
    if (!entity || !entity.id) {
      return '';
    }
    return (
      <Advanced.WorkflowProcessInfo entityIdentifier={entity.id}/>
    );
  }

  render() {
    const { uiKey, workflowHistoricProcessInstanceManager, columns, forceSearchParameters } = this.props;
    const { filterOpened } = this.state;
    return (
      <div>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          forceSearchParameters={forceSearchParameters}
          manager={workflowHistoricProcessInstanceManager}
          showRowSelection={false}
          showId
          filter={this._filter()}
          filterOpened={filterOpened}
          _searchParameters={ this.getSearchParameters() }>

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
          <Advanced.Column
            header=""
            property="name"
            cell={this._getWfProcessCell}
            sort={false}
            rendered={_.includes(columns, 'name')}/>
          <Advanced.Column property="startTime" sort face="datetime" rendered={_.includes(columns, 'startTime')}/>
          <Advanced.Column property="endTime" sort face="datetime" rendered={_.includes(columns, 'endTime')}/>
          <Advanced.Column property="startActivityId" sort={false} face="text" rendered={_.includes(columns, 'startActivityId')}/>
          <Advanced.Column property="deleteReason" sort={false} face="text" rendered={_.includes(columns, 'deleteReason')}/>
          <Advanced.Column property="superProcessInstanceId" sort={false} face="text" rendered={_.includes(columns, 'superProcessInstanceId')}/>
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
  columns: ['deleteReason', 'id', 'endTime', 'startTime', 'name'],
  filterOpened: false,
  _showLoading: false
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    _showLoading: component.workflowHistoricProcessInstanceManager.isShowLoading(state, `${component.uiKey}-detail`)
  };
}

export default connect(select, null, null, { forwardRef: true })(HistoricProcessInstanceTable);
