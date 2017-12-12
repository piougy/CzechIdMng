import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import OperationStateEnum from '../../enums/OperationStateEnum';
import { SecurityManager } from '../../redux';

/**
 * Table with long running tasks
 *
 * @author Radek Tomiška
 */
class LongRunningTaskTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    // default filter status
    // true - open
    // false - close
    this.state = {
      detail: {
        show: false,
        entity: null
      },
      filterOpened: true
    };
  }

  getContentKey() {
    return 'content.scheduler.all-tasks';
  }

  getManager() {
    return this.props.manager;
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

  /**
   * Shows LRT detail with given entity
   */
   showDetail(entity) {
     this.context.router.push(`/scheduler/all-tasks/${encodeURIComponent(entity.id)}/detail`);
   }

  /**
   * Bulk action - process selected created tasks
   *
   * @param  {string} bulkActionValue 'processCreated'
   * @param  {arrayOf(string)} tasks tasks ids
   */
  onProcessCreated(bulkActionValue, tasks) {
    let i;
    const { manager } = this.props;
    // TODO - bulk action should be used - see #onDelete()
    for ( i = 0; i < tasks.length; i++ ) {
      this.context.store.dispatch(manager.processCreatedTask( tasks[i], 'task-queue-process-created', () => {
        this.addMessage({ level: 'success', message: this.i18n('action.processCreated.success')});
      }));
    }
  }

  /**
   * Close modal detail
   */
  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: null
      }
    });
  }

  /**
   * Process all created tasks from task's queue
   */
  processCreated() {
    const { manager } = this.props;
    //
    this.context.store.dispatch(manager.processCreated('task-queue-process-created', () => {
      this.addMessage({ level: 'success', message: this.i18n('action.processCreated.success') });
    }));
  }

  /**
   * Bulk delete operation
   */
  onCancel(bulkActionValue, selectedRows) {
    const selectedEntities = this.getManager().getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: this.getManager().getNiceLabel(selectedEntities[0]), records: this.getManager().getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: this.getManager().getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      this.context.store.dispatch(this.getManager().cancelEntities(selectedEntities, this.getUiKey(), (entity, error) => {
        if (entity && error) {
          if (error.statusCode !== 202) {
            this.addErrorMessage({ title: this.i18n(`action.delete.error`, { record: this.getManager().getNiceLabel(entity) }) }, error);
          } else {
            this.addError(error);
          }
        } else {
          this.refs.table.getWrappedInstance().reload();
        }
      }));
    }, () => {
      // nothing
    });
  }

  render() {
    const { uiKey, manager, showRowSelection } = this.props;
    const { filterOpened } = this.state;
    return (
      <div>
        <Basic.Confirm ref="confirm-cancel" level="danger"/>

        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={manager}
          showRowSelection={showRowSelection}
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row>
                  <div className="col-lg-4">
                    <Advanced.Filter.DateTimePicker
                      mode="date"
                      ref="from"
                      placeholder={this.i18n('filter.dateFrom.placeholder')}/>
                  </div>
                  <div className="col-lg-4">
                    <Advanced.Filter.DateTimePicker
                      mode="date"
                      ref="till"
                      placeholder={this.i18n('filter.dateTill.placeholder')}/>
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
                      enum={OperationStateEnum}/>
                  </div>
                  <div className="col-lg-4">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.text.placeholder')}/>
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened={filterOpened}
          actions={
            [
              { value: 'processCreated', niceLabel: this.i18n('action.processCreated.selectedButton'), action: this.onProcessCreated.bind(this) },
              { value: 'cancel', niceLabel: this.i18n('action.cancel.action'), action: this.onCancel.bind(this) }
            ]
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="start-button"
                type="submit"
                className="btn-xs"
                rendered={SecurityManager.hasAuthority('SCHEDULER_EXECUTE')}
                onClick={ this.processCreated.bind(this) }>
                <Basic.Icon icon="play"/>
                {' '}
                { this.i18n('action.processCreated.button') }
              </Basic.Button>
            ]
          }
          _searchParameters={ this.getSearchParameters() }>

          <Advanced.Column
            property=""
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex])}/>
                );
              }
            }/>
          <Advanced.Column property="result.state" width={75} header={this.i18n('entity.LongRunningTask.result.state')} sort face="enum" enumClass={OperationStateEnum}/>
          <Advanced.Column property="created" width={150} header={this.i18n('entity.created')} sort face="datetime"/>
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
          <Advanced.Column
            property="taskProperties"
            header={ this.i18n('entity.LongRunningTask.taskProperties.label') }
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data, property }) => {
                const entity = data[rowIndex];
                const propertyValue = entity[property];
                return _.keys(propertyValue).map(propertyName => {
                  return (
                    <div>{ propertyName }: { '' + propertyValue[propertyName] }</div>
                  );
                });
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
                  <span>{manager.getProcessedCount(entity)}</span>
                );
              }
            }/>
        </Advanced.Table>
      </div>
    );
  }
}

LongRunningTaskTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object.isRequired,
  showRowSelection: PropTypes.boolean
};

LongRunningTaskTable.defaultProps = {
  showRowSelection: true
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(LongRunningTaskTable);
