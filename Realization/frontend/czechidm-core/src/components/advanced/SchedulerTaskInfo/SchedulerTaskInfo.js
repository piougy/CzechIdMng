import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import { SchedulerManager, DataManager } from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';
import LongRunningTaskName from '../LongRunningTask/LongRunningTaskName';
import LongRunningTaskProperties from '../LongRunningTask/LongRunningTaskProperties';

const manager = new SchedulerManager();

/**
 * Scheduler task basic information (info card)
 *
 * @author Radek Tomiška
 */
export class SchedulerTaskInfo extends AbstractEntityInfo {

  componentDidMount() {
    super.componentDidMount();
    //
    this.context.store.dispatch(manager.fetchSupportedTasks());
  }

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    //
    // evaluate authorization policies
    const { _permissions } = this.props;
    if (!manager.canRead(this.getEntity(), _permissions)) {
      return false;
    }
    return true;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'component:scheduled-task';
  }

  getNiceLabel() {
    const _entity = this.getEntity();
    //
    return this.getManager().getNiceLabel(_entity, false);
  }

  /**
   * Renders nicelabel used in text and link face
   */
  _renderNiceLabel(entity) {
    const { className, style, supportedTasks } = this.props;
    const _entity = entity || this.getEntity();
    //
    return (
      <LongRunningTaskName entity={ _entity } supportedTasks={ supportedTasks } className={ className } style={ style }/>
    );
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('entity.SchedulerTask._type');
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    return [
      {
        label: this.i18n('entity.SchedulerTask.taskType'),
        value: this.getNiceLabel()
      },
      {
        label: this.i18n('entity.SchedulerTask.description'),
        value: entity.description
      },
      {
        label: this.i18n('entity.SchedulerTask.parameters.label'),
        value: (
          <LongRunningTaskProperties entity={ entity } supportedTasks={ this.props.supportedTasks } condensed/>
        )
      }
    ];
  }
}

SchedulerTaskInfo.propTypes = {
  ...AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority
   */
  entity: PropTypes.object,
  /**
   * Selected entity's id - entity will be loaded automatically
   */
  entityIdentifier: PropTypes.string,
  //
  _showLoading: PropTypes.bool,
  /**
   * Supported schedulable long running task.
   */
  supportedTasks: PropTypes.arrayOf(PropTypes.object)
};
SchedulerTaskInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'link',
  _showLoading: true,
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.entityIdentifier),
    _showLoading: manager.isShowLoading(state, null, component.entityIdentifier),
    supportedTasks: DataManager.getData(state, SchedulerManager.UI_KEY_SUPPORTED_TASKS)
  };
}
export default connect(select)(SchedulerTaskInfo);
