import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import OperationStateEnum from '../../enums/OperationStateEnum';
import { SecurityManager, ConfigurationManager } from '../../redux';

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
    this.refs.table.useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.cancelFilter(this.refs.filterForm);
  }

  /**
   * Shows LRT detail with given entity
   */
  showDetail(entity) {
    this.context.history.push(`/scheduler/all-tasks/${encodeURIComponent(entity.id)}/detail`);
  }

  /**
   * Bulk action - process selected created tasks
   *
   * @param  {string} bulkActionValue 'processCreated'
   * @param  {arrayOf(string)} tasks tasks ids
   */
  onProcessCreated(bulkActionValue, tasks) {
    // TODO - bulk action should be used - see #onDelete()
    for (let i = 0; i < tasks.length; i++) {
      this.context.store.dispatch(this.getManager().processCreatedTask(tasks[i], 'task-queue-process-created', () => {
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
          this.refs.table.reload();
        }
      }));
    }, () => {
      // nothing
    });
  }

  onRun(entity, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs['confirm-task-run'].show(
      this.i18n(`action.task-run.message`, { record: this.getManager().getNiceLabel(entity) }),
      this.i18n(`action.task-run.header`)
    ).then(() => {
      this.context.store.dispatch(this.getManager().processCreatedTask(entity.id, 'task-queue-process-created', () => {
        this.addMessage({ message: this.i18n('action.task-run.success', { count: 1, record: this.getManager().getNiceLabel(entity) }) });
        this.refs.table.reload();
      }));
    }, () => {
      // Rejected
    });
  }

  onRecover(entity, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs['confirm-task-recover'].show(
      this.i18n(`action.task-recover.message`, { record: this.getManager().getNiceLabel(entity) }),
      this.i18n(`action.task-recover.header`)
    ).then(() => {
      this.context.store.dispatch(this.getManager().recover(entity, 'task-queue-process-created', () => {
        this.addMessage({ message: this.i18n('action.task-recover.success', { count: 1, record: this.getManager().getNiceLabel(entity) }) });
        this.refs.table.reload();
      }));
    }, () => {
      // Rejected
    });
  }

  /**
   * FIXME: rename after #2066
   */
  onCancelTask(entity, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs['confirm-task-cancel'].show(
      this.i18n(`action.task-cancel.message`, { record: this.getManager().getNiceLabel(entity) }),
      this.i18n(`action.task-cancel.header`)
    ).then(() => {
      this.context.store.dispatch(this.getManager().cancel(entity, 'task-queue-process-created', () => {
        this.addMessage({
          level: 'info',
          message: this.i18n('action.task-cancel.success', { count: 1, record: this.getManager().getNiceLabel(entity) })
        });
        this.refs.table.reload();
      }));
    }, () => {
      // Rejected
    });
  }

  render() {
    const { showRowSelection, showTransactionId } = this.props;
    const { filterOpened } = this.state;
    return (
      <Basic.Div>
        <Basic.Confirm ref="confirm-cancel" level="warning"/>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Basic.Confirm ref="confirm-task-run" level="success"/>
        <Basic.Confirm ref="confirm-task-recover" level="warning">
          <Basic.Alert icon="warning-sign" level="warning" text={ this.i18n(`action.task-recover.warning.base`) } style={{ marginTop: 15 }}/>
        </Basic.Confirm>
        <Basic.Confirm ref="confirm-task-cancel" level="warning"/>

        <Advanced.Table
          ref="table"
          uiKey={ this.getUiKey() }
          manager={ this.getManager() }
          showRowSelection={ showRowSelection && SecurityManager.hasAnyAuthority(['SCHEDULER_UPDATE', 'SCHEDULER_EXECUTE']) }
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row>
                  <Basic.Col lg={ 8 }>
                    <Advanced.Filter.FilterDate ref="fromTill"/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </Basic.Col>
                </Basic.Row>
                <Basic.Row className="last">
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.EnumSelectBox
                      ref="operationState"
                      placeholder={this.i18n('filter.operationState.placeholder')}
                      enum={OperationStateEnum}/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.text.placeholder')}/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 } rendered={ showTransactionId }>
                    <Advanced.Filter.TextField
                      ref="transactionId"
                      placeholder={ this.i18n('filter.transactionId.placeholder') }/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened={filterOpened}
          actions={
            [
              { value: 'processCreated', niceLabel: this.i18n('action.processCreated.selectedButton'), action: this.onProcessCreated.bind(this), rendered: SecurityManager.hasAuthority('SCHEDULER_EXECUTE') },
              { value: 'cancel', niceLabel: this.i18n('action.cancel.action'), action: this.onCancel.bind(this), rendered: SecurityManager.hasAuthority('SCHEDULER_UPDATE') },
              { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), rendered: SecurityManager.hasAuthority('LONGRUNNINGTASK_DELETE'), disabled: false }
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
          <Advanced.Column
            property="result.state"
            width={75}
            header={this.i18n('entity.LongRunningTask.result.state')}
            sort
            cell={
              ({ data, rowIndex }) => {
                const entity = data[rowIndex];
                if (!entity.result || !entity.result.state) {
                  return null;
                }
                let label = null;
                if (OperationStateEnum.findSymbolByKey(entity.result.state) === OperationStateEnum.RUNNING && !entity.running) {
                  // task is prepared for execution in the queue
                  label = this.i18n('label.waiting');
                } else if (OperationStateEnum.findSymbolByKey(entity.result.state) === OperationStateEnum.RUNNING) {
                  label = this.getManager().getProcessedCount(entity);
                }
                //
                return (
                  <Advanced.OperationResult
                    value={ entity.result }
                    stateLabel={ label }
                    detailLink={ () => this.showDetail(data[rowIndex]) }/>
                );
              }
            }/>
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
                return [..._.keys(propertyValue).map(propertyName => {
                  if (Utils.Ui.isEmpty(propertyValue[propertyName])) {
                    return null;
                  }
                  if (propertyName === 'core:transactionContext') {
                    // FIXME: transaction context info
                    return null;
                  }
                  return (
                    <div>{ propertyName }: { Utils.Ui.toStringValue(propertyValue[propertyName]) }</div>
                  );
                }).values()];
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
                  <Advanced.LongRunningTask entity={ entity } face="count"/>
                );
              }
            }/>
          <Basic.Column
            header={ this.i18n('label.action') }
            className="action"
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                const isCreated = entity.resultState === 'CREATED';
                const isRunning = entity.resultState === 'RUNNING';

                //
                return (
                  <Basic.Div>
                    <Basic.Button
                      level="warning"
                      className="btn-xs"
                      title={ this.i18n('button.cancel') }
                      titlePlacement="bottom"
                      rendered={ entity.running && SecurityManager.hasAnyAuthority(['SCHEDULER_EXECUTE']) }
                      onClick={ this.onCancelTask.bind(this, entity) }
                      icon="fa:cog fa-spin"/>
                    <Basic.Button
                      level="success"
                      className="btn-xs"
                      title={ this.i18n('button.run') }
                      titlePlacement="bottom"
                      rendered={ isCreated && !entity.running && !isRunning && SecurityManager.hasAnyAuthority(['SCHEDULER_EXECUTE']) }
                      onClick={ this.onRun.bind(this, entity) }
                      icon="play"/>
                    <Basic.Button
                      level="warning"
                      className="btn-xs"
                      title={ !entity.recoverable ? this.i18n('button.recover.disabled') : this.i18n('button.recover.title') }
                      titlePlacement="bottom"
                      rendered={ !isCreated && !entity.running && !isRunning && SecurityManager.hasAnyAuthority(['SCHEDULER_EXECUTE']) }
                      disabled={ !entity.recoverable }
                      onClick={ this.onRecover.bind(this, entity) }
                      icon="play"/>
                  </Basic.Div>
                );
              }
            }/>
        </Advanced.Table>
      </Basic.Div>
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
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    showTransactionId: ConfigurationManager.showTransactionId(state)
  };
}

export default connect(select)(LongRunningTaskTable);
