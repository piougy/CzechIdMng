import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import moment from 'moment';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { SchedulerManager, DataManager, ConfigurationManager } from '../../redux';
import * as Utils from '../../utils';
import TriggerTypeEnum from '../../enums/TriggerTypeEnum';

const manager = new SchedulerManager();

/**
 * Scheduler administration
 *
 * @author Radek Tomiška
 */
class ScheduleTasks extends Basic.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      ...this.state,
      triggerDetail: {
        show: false,
        entity: {}
      },
      triggerType: TriggerTypeEnum.findKeyBySymbol(TriggerTypeEnum.SIMPLE),
      taskType: null
    };
  }

  getContentKey() {
    return 'content.scheduler.schedule-tasks';
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return SchedulerManager.UI_KEY_TASKS;
  }

  componentDidMount() {
    this.selectNavigationItems(['system', 'scheduler', 'scheduler-schedule-tasks']);
    this.context.store.dispatch(manager.fetchSupportedTasks());
    this.context.store.dispatch(manager.fetchTasks());
  }

  /**
   * Open modal task detail
   */
  showDetail(entity) {
    // transform parameters to Form
    if (entity.parameters) {
      _.keys(entity.parameters).map(parameterName => {
        entity[`parameter-${parameterName}`] = entity.parameters[parameterName];
      });
    }
    super.showDetail(entity, () => {
      this.refs.taskType.focus();
    });
  }

  /**
   * Close modal task detail
   */
  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: {}
      },
      taskType: null
    });
  }

  onChangeTaskType(taskType) {
    this.setState({
      taskType
    });
  }

  /**
   * Saves give entity
   */
  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const { taskType } = this.state;
    const entity = this.refs.form.getData();
    // transform parameters
    if (taskType.parameters) {
      entity.parameters = {};
      _.keys(taskType.parameters).map(parameterName => {
        entity.parameters[parameterName] = this.refs[`parameter-${parameterName}`].getValue();
      });
    }
    //
    if (entity.id === undefined) {
      this.context.store.dispatch(this.getManager().createEntity(entity, SchedulerManager.UI_KEY_TASKS, this.afterSave.bind(this)));
    } else {
      this.context.store.dispatch(this.getManager().patchEntity(entity, SchedulerManager.UI_KEY_TASKS, this.afterSave.bind(this)));
    }
  }

  /**
   * Callback after save
   */
  afterSave(entity, error) {
    if (error) {
      this.addError(error);
      this.refs.form.processEnded();
      return;
    }
    this.addMessage({ message: this.i18n('action.save.success', { record: this.getManager().getNiceLabel(entity) }) });
    this.context.store.dispatch(manager.fetchTasks());
    this.closeDetail();
  }

  onDelete(entity, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs['confirm-delete'].show(
      this.i18n(`action.delete.message`, { count: 1, record: this.getManager().getNiceLabel(entity) }),
      this.i18n(`action.delete.header`, { count: 1 })
    ).then(() => {
      this.context.store.dispatch(this.getManager().deleteEntity(entity, SchedulerManager.UI_KEY_TASKS, (deletedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n('action.delete.success', { count: 1, record: this.getManager().getNiceLabel(entity) }) });
        } else {
          this.addError(error);
        }
        this.context.store.dispatch(manager.fetchTasks());
      }));
    }, () => {
      // Rejected
    });
  }

  onRun(entity, event) {
    if (event) {
      event.preventDefault();
    }
    this.context.store.dispatch(this.getManager().runTask(entity.id, () => {
      this.addMessage({ message: this.i18n('action.task-run.success', { count: 1, record: this.getManager().getNiceLabel(entity) }) });
    }));
  }

  onTriggerDelete(trigger) {
    const record = moment(trigger.nextFireTime).format(this.i18n('format.datetime'));
    // show confirm message for deleting entity or entities
    this.refs['confirm-delete'].show(
      this.i18n(`action.trigger-delete.message`, { record }),
      this.i18n(`action.trigger-delete.header`)
    ).then(() => {
      this.context.store.dispatch(manager.deleteTrigger(trigger, () => {
        this.addMessage({ message: this.i18n(`action.trigger-delete.success`, { record }) });
      }));
    }, () => {
      //
    });
  }

  saveTrigger(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.triggerForm.isFormValid()) {
      return;
    }
    const formEntity = this.refs.triggerForm.getData();
    //
    this.context.store.dispatch(this.getManager().createTrigger(formEntity, () => {
      this.addMessage({ message: this.i18n('action.trigger-create.success') });
      this.closeTriggerDetail();
    }));
  }


  onChangeTriggerType(triggerType) {
    this.setState({
      triggerType: triggerType.value
    });
  }

  /**
   * Shows modal detail with given entity
   */
  showTriggerDetail(entity) {
    this.setState({
      triggerDetail: {
        show: true,
        entity
      }
    }, () => {
      this.refs.triggerForm.setData(entity);
      this.refs.type.focus();
    });
  }

  /**
   * Close modal detail
   */
  closeTriggerDetail() {
    this.setState({
      triggerDetail: {
        show: false,
        entity: {}
      }
    });
  }

  render() {
    const { supportedTasks, tasks, showLoading, instanceId } = this.props;
    const { detail, triggerDetail, triggerType, taskType } = this.state;
    //
    const _tasks = [];
    if (tasks) {
      tasks.forEach(task => {
        _tasks.push(task);
      });
    }
    _tasks.sort((one, two) => {
      return one.taskType > two.taskType;
    });
    const _supportedTasks = [];
    if (supportedTasks) {
      supportedTasks.forEach(task => {
        _supportedTasks.push({
          niceLabel: this.getManager().getSimpleTaskType(task.taskType),
          value: task.taskType,
          description: task.description,
          parameters: task.parameters
        });
      });
    }
    //
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.Toolbar>
          <div className="pull-left">

          </div>
          <div className="pull-right">
            <Basic.Button
              level="success"
              key="add_button"
              className="btn-xs"
              onClick={ this.showDetail.bind(this, _supportedTasks.length === 0 ? {} : { name: _supportedTasks[0], instanceId }) }
              rendered={ _supportedTasks.length > 0 }>
              <Basic.Icon type="fa" icon="plus"/>
              {' '}
              {this.i18n('button.add')}
            </Basic.Button>
          </div>
          <div className="clearfix"></div>
        </Basic.Toolbar>

        <Basic.Table
          ref="table"
          data={_tasks}
          showLoading={showLoading}
          noData={this.i18n('component.basic.Table.noData')}>
          <Basic.Column
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
          <Basic.Column property="id" header={this.i18n('entity.ScheduleTask.id')} rendered={false} />
          <Basic.Column
            property="taskType"
            width={150}
            header={this.i18n('entity.ScheduleTask.taskType')}
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data, property }) => {
                const propertyValue = data[rowIndex][property];
                return (
                  <span title={propertyValue}>{ this.getManager().getSimpleTaskType(propertyValue) }</span>
                );
              }
            }/>
          <Basic.Column property="description" header={this.i18n('entity.ScheduleTask.description')} />
          <Basic.Column property="instanceId" header={this.i18n('entity.ScheduleTask.instanceId.label')} />
          <Basic.Column
            header={this.i18n('action.task-edit.parameters')}
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                if (!entity.parameters) {
                  return null;
                }
                return _.keys(entity.parameters).map(parameterName => {
                  if (parameterName.lastIndexOf('core:', 0) === 0) {
                    return null;
                  }
                  return (<div>{parameterName}: {entity.parameters[parameterName]}</div>);
                });
              }
            }/>
          <Basic.Column
            property="triggers"
            header={this.i18n('entity.ScheduleTask.triggers')}
            cell={
              ({ data, rowIndex, property}) => {
                const triggers = data[rowIndex][property];
                return (
                  <div>
                    {
                      triggers.map(trigger => {
                        if (!trigger.nextFireTime || (trigger.fireTime && moment(trigger.fireTime).isBefore(moment()))) {
                          // only plan
                          return null;
                        }
                        return (
                          <div>
                            <Advanced.DateValue value={trigger.nextFireTime} showTime />
                            {' '}
                            <Basic.Button
                              level="link"
                              className="btn-xs"
                              onClick={this.onTriggerDelete.bind(this, trigger)}>
                              <Basic.Icon value="remove" color="red"/>
                              </Basic.Button>
                          </div>
                        );
                      })
                    }
                    <Basic.Button
                      level="success"
                      className="btn-xs"
                      onClick={this.showTriggerDetail.bind(this, { type: triggerType, taskId: data[rowIndex].id })}>
                      <Basic.Icon value="plus"/>
                      {' '}
                      { this.i18n('button.add') }
                    </Basic.Button>
                  </div>
                );
              }
            }/>
            <Basic.Column
              header={this.i18n('label.action')}
              className="action"
              cell={
                ({rowIndex, data}) => {
                  return (
                    <div>
                      <Basic.Button
                        level="danger"
                        onClick={this.onDelete.bind(this, data[rowIndex])}
                        className="btn-xs"
                        title={this.i18n('button.delete')}
                        titlePlacement="bottom">
                        <Basic.Icon icon="trash"/>
                      </Basic.Button>
                      <Basic.Button
                        level="success"
                        onClick={this.onRun.bind(this, data[rowIndex])}
                        className="btn-xs"
                        title={this.i18n('button.run')}
                        titlePlacement="bottom"
                        style={{ marginLeft: 3 }}>
                        <Basic.Icon icon="play"/>
                      </Basic.Button>
                    </div>
                  );
                }
              }/>
        </Basic.Table>

        <Basic.Modal
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!showLoading}>

          <form onSubmit={this.save.bind(this)}>
            <Basic.Modal.Header closeButton={!showLoading} text={detail.entity.id !== undefined ? this.i18n('action.task-edit.header') : this.i18n('action.task-create.header')}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm ref="form" showLoading={showLoading} className="form-horizontal" readOnly={detail.entity.id !== undefined}>
                <Basic.EnumSelectBox
                  ref="taskType"
                  label={this.i18n('entity.ScheduleTask.taskType')}
                  options={_supportedTasks}
                  onChange={this.onChangeTaskType.bind(this)}
                  required/>
                <Basic.TextArea
                  ref="description"
                  placeholder={taskType ? taskType.description : null}
                  label={this.i18n('entity.ScheduleTask.description')}
                  max={255}/>
                <Basic.TextField
                  ref="instanceId"
                  label={this.i18n('entity.ScheduleTask.instanceId.label')}
                  helpBlock={this.i18n('entity.ScheduleTask.instanceId.help')}
                  required/>
                {
                  (detail.entity.id === undefined && taskType && taskType.parameters)
                  ?
                  <div>
                    <Basic.ContentHeader text={this.i18n('action.task-edit.parameters')} />
                    {
                      _.keys(taskType.parameters).map(parameterName => {
                        return (
                          <Basic.TextField
                            label={parameterName}
                            ref={`parameter-${parameterName}`}
                            value={taskType.parameters[parameterName]}
                            max={255}/>
                        );
                      })
                    }
                  </div>
                  :
                  <div>
                    {
                      !detail.entity.parameters
                      ||
                      <div>
                        <Basic.ContentHeader text={this.i18n('action.task-edit.parameters')} />
                          {
                            _.keys(detail.entity.parameters).map(parameterName => {
                              if (parameterName.lastIndexOf('core:', 0) === 0) {
                                return null;
                              }
                              return (
                                <Basic.TextField
                                  label={parameterName}
                                  ref={`parameter-${parameterName}`}
                                  max={255}/>
                              );
                            })
                          }
                      </div>
                    }
                  </div>
                }
              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={this.closeDetail.bind(this)}
                showLoading={showLoading}>
                {this.i18n('button.close')}
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}
                rendered={detail.entity.id === undefined}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>

        <Basic.Modal
          show={triggerDetail.show}
          onHide={this.closeTriggerDetail.bind(this)}
          backdrop="static"
          keyboard={!showLoading}>

          <form onSubmit={this.saveTrigger.bind(this)}>
            <Basic.Modal.Header closeButton={!showLoading} text={this.i18n('action.trigger-create.header')}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm ref="triggerForm" showLoading={showLoading} className="form-horizontal">
                <Basic.EnumSelectBox
                  ref="type"
                  enum={TriggerTypeEnum}
                  label={this.i18n('entity.ScheduleTask.trigger._type.label')}
                  required
                  onChange={this.onChangeTriggerType.bind(this)}/>
                <Basic.DateTimePicker
                  ref="fireTime"
                  label={this.i18n('entity.ScheduleTask.trigger.fireTime')}
                  hidden={triggerType !== 'SIMPLE'}
                  required={triggerType === 'SIMPLE'}/>
                <Basic.TextField
                  ref="cron"
                  label={this.i18n('entity.ScheduleTask.trigger.cron.label')}
                  helpBlock={<span dangerouslySetInnerHTML={{__html: this.i18n('entity.ScheduleTask.trigger.cron.help')}}/>}
                  hidden={triggerType !== 'CRON'}
                  required={triggerType === 'CRON'}/>
              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={this.closeTriggerDetail.bind(this)}
                showLoading={showLoading}>
                {this.i18n('button.close')}
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </div>
    );
  }
}

ScheduleTasks.propTypes = {
  instanceId: PropTypes.string,
  userContext: PropTypes.object,
  tasks: PropTypes.object,
  supportedTasks: PropTypes.object,
  showLoading: PropTypes.bool
};
ScheduleTasks.defaultProps = {
  instanceId: null,
  userContext: null,
  tasks: null,
  supportedTasks: null,
  showLoading: true
};

function select(state) {
  return {
    instanceId: ConfigurationManager.getPublicValue(state, 'idm.pub.app.instanceId'),
    userContext: state.security.userContext,
    tasks: DataManager.getData(state, SchedulerManager.UI_KEY_TASKS),
    supportedTasks: DataManager.getData(state, SchedulerManager.UI_KEY_SUPPORTED_TASKS),
    showLoading: Utils.Ui.isShowLoading(state, SchedulerManager.UI_KEY_TASKS) || Utils.Ui.isShowLoading(state, SchedulerManager.UI_KEY_SUPPORTED_TASKS),
  };
}

export default connect(select)(ScheduleTasks);
