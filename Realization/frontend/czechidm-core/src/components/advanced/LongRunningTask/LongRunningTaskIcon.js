import React from 'react';
import PropTypes from 'prop-types';
//
import * as Basic from '../../basic';
import { FormAttributeManager } from '../../../redux';

const formAttributeManager = new FormAttributeManager();

/**
 * Task icon - if form definition is available.
 *
 * @author Radek Tomiška
 * @since 10.7.0
 */
export default class LongRunningTaskIcon extends Basic.AbstractContextComponent {

  render() {
    const { rendered, showLoading, entity, supportedTasks, className, style } = this.props;
    //
    if (!rendered) {
      return null;
    }
    //
    if (showLoading) {
      return (
        <Basic.Icon value="fa:refresh" showLoading/>
      );
    }
    if (!entity) {
      return null;
    }
    //
    let _taskType;
    if (supportedTasks && supportedTasks.has(entity.taskType)) {
      _taskType = supportedTasks.get(entity.taskType);
    } else if (entity.taskProperties) {
      const bulkAction = entity.taskProperties['core:bulkAction'];
      // try to find form attributes from form definition
      if (bulkAction) {
        _taskType = {
          formDefinition: {
            code: bulkAction.name,
            module: bulkAction.module,
            type: 'bulk-action'
          }
        };
      }
    }
    let _icon = 'component:scheduled-task';
    if (_taskType && _taskType.formDefinition) {
      _icon = formAttributeManager.getLocalization(_taskType.formDefinition, null, 'icon', _icon);
    }
    //
    return (
      <Basic.Icon value={ _icon } className={ className } style={ style }/>
    );
  }
}

LongRunningTaskIcon.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * LRT
   *
   * @type {IdmLongRunningTaskDto}
   */
  entity: PropTypes.object,
  /**
   * Supported schedulable long running task.
   */
  supportedTasks: PropTypes.arrayOf(PropTypes.object),
};
LongRunningTaskIcon.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps
};
