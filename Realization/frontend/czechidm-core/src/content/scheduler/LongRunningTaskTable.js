import React, { PropTypes } from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import OperationStateEnum from '../../enums/OperationStateEnum';

/**
 * Table with long running tasks
 *
 * @author Radek Tomiška
 */
export default class LongRunningTaskTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    // default filter status
    // true - open
    // false - close
    this.state = {
      filterOpened: false
    };
  }

  getContentKey() {
    return 'content.scheduler.all-tasks';
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
    const { uiKey, manager } = this.props;
    const { filterOpened } = this.state;

    return (
      <Advanced.Table
        ref="table"
        uiKey={uiKey}
        manager={manager}
        filter={
          <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
            <Basic.AbstractForm ref="filterForm" className="form-horizontal">
              <Basic.Row>
                <div className="col-lg-4">
                  <Advanced.Filter.DateTimePicker
                    mode="date"
                    ref="from"
                    placeholder={this.i18n('filter.dateFrom.placeholder')}
                    label={this.i18n('filter.dateFrom.label')}/>
                </div>
                <div className="col-lg-4">
                  <Advanced.Filter.DateTimePicker
                    mode="date"
                    ref="till"
                    placeholder={this.i18n('filter.dateTill.placeholder')}
                    label={this.i18n('filter.dateTill.label')}/>
                </div>
                <div className="col-lg-4 text-right">
                  <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                </div>
              </Basic.Row>
              <Basic.Row className="last">
                <div className="col-lg-4">
                  <Advanced.Filter.EnumSelectBox
                    ref="operationState"
                    placeholder={this.i18n('filter.operationState.placeholder')}
                    label={this.i18n('filter.operationState.label')}
                    enum={OperationStateEnum}/>
                </div>
                <div className="col-lg-4">
                  <Advanced.Filter.TextField
                    ref="text"
                    placeholder={this.i18n('filter.text.placeholder')}
                    label={this.i18n('filter.text.label')}/>
                </div>
              </Basic.Row>
            </Basic.AbstractForm>
          </Advanced.Filter>
        }
        filterOpened={!filterOpened}>
        <Advanced.Column property="result.state" width={75} header={this.i18n('entity.LongRunningTask.result.state')} sort face="enum" enumClass={OperationStateEnum}/>
        <Advanced.Column property="created" width={150} header={this.i18n('entity.created')} sort face="datetime"/>
        <Advanced.Column property="instanceId" width={150} header={this.i18n('entity.ScheduleTask.instanceId.label')} sort face="text"/>
        <Advanced.Column property="taskId" sort rendered={false}/>
        <Advanced.Column
          property="taskType"
          width={150}
          sort
          cell={
            /* eslint-disable react/no-multi-comp */
            ({ rowIndex, data, property }) => {
              const entity = data[rowIndex];
              const propertyValue = entity[property];
              return (
                <span title={propertyValue}>{ propertyValue.split('.').pop(-1) }</span>
              );
            }
          }/>
        <Advanced.Column property="taskDescription" sort />
        <Advanced.Column
          property="counter"
          width={75}
          sort
          cell={
            /* eslint-disable react/no-multi-comp */
            ({ rowIndex, data }) => {
              const entity = data[rowIndex];
              return (
                <span>{entity.counter !== null ? entity.counter : '?'} / {entity.count !== null ? entity.count : '?'}</span>
              );
            }
          }/>
      </Advanced.Table>
    );
  }
}

LongRunningTaskTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object.isRequired
};

LongRunningTaskTable.defaultProps = {
};
