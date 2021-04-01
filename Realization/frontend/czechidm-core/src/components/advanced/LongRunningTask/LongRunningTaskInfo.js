import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import { LongRunningTaskManager, DataManager, SchedulerManager, SecurityManager } from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';
import DateValue from '../DateValue/DateValue';
import LongRunningTaskName from './LongRunningTaskName';
import OperationResult from '../OperationResult/OperationResult';

const manager = new LongRunningTaskManager();
const schedulerManager = new SchedulerManager();

/**
 * Long running task basic information (info card).
 *
 * @author Radek Tomiška
 * @since 10.7.0
 */
export class LongRunningTaskInfo extends AbstractEntityInfo {

  componentDidMount() {
    super.componentDidMount();
    //
    if (SecurityManager.hasAuthority('SCHEDULER_READ')) {
      this.context.store.dispatch(schedulerManager.fetchSupportedTasks());
    }
  }

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    const { _permissions } = this.props;
    if (!manager.canRead(this.getEntity(), _permissions)) {
      return false;
    }
    return true;
  }

  getNiceLabel(entity) {
    const { _supportedTasks } = this.props;
    const _entity = entity || this.getEntity();
    //
    let value = this.getManager().getNiceLabel(_entity, _supportedTasks, false);
    if (value.length > 60) {
      value = `${ value.substr(0, 60) }...`;
    }
    if (!value) {
      return this.i18n('entity.LongRunningTask._type');
    }

    return value;
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('entity.LongRunningTask._type');
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'component:scheduled-task';
  }

  /**
   * Get link to detail (`url`).
   *
   * @return {string}
   */
  getLink() {
    const entity = this.getEntity();
    //
    return `/scheduler/all-tasks/${ encodeURIComponent(entity.id) }/detail`;
  }

  getTableChildren() {
    // component are used in #getPopoverContent => skip default column resolving
    return [
      <Basic.Column property="label"/>,
      <Basic.Column property="value"/>
    ];
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    const { _supportedTasks } = this.props;
    //
    const content = [
      {
        label: this.i18n('entity.LongRunningTask.taskType'),
        value: (
          <LongRunningTaskName entity={ entity } supportedTasks={ _supportedTasks }/>
        )
      },
      {
        label: this.i18n('entity.LongRunningTask.started'),
        value: (
          <DateValue value={ entity.taskStarted }/>
        )
      },
      {
        label: this.i18n('entity.LongRunningTask.dryRun.label'),
        value: (entity.dryRun ? this.i18n('label.yes') : this.i18n('label.no'))
      },
      {
        label: this.i18n('entity.LongRunningTask.result.state'),
        value: (<OperationResult value={ entity.result }/>)
      }
    ];
    //
    if (entity.taskDescription) {
      content.push(
        {
          label: this.i18n('entity.LongRunningTask.taskDescription'),
          value: entity.taskDescription
        }
      );
    }
    //
    return content;
  }
}

LongRunningTaskInfo.propTypes = {
  ...AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority
   */
  entity: PropTypes.object,
  /**
   * Selected entity's id - entity will be loaded automatically
   */
  entityIdentifier: PropTypes.string,
  /**
   * Internal entity loaded by given identifier
   */
  _entity: PropTypes.object,
  _showLoading: PropTypes.bool
};
LongRunningTaskInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'link',
  _showLoading: true,
};

function select(state, component) {
  const { entityIdentifier, entity } = component;
  let entityId = entityIdentifier;
  if (!entityId && entity) {
    entityId = entity.id;
  }
  //
  return {
    _entity: manager.getEntity(state, component.entityIdentifier),
    _showLoading: manager.isShowLoading(state, null, component.entityIdentifier),
    _permissions: manager.getPermissions(state, null, entityId),
    _supportedTasks: DataManager.getData(state, SchedulerManager.UI_KEY_SUPPORTED_TASKS)
  };
}
export default connect(select)(LongRunningTaskInfo);
