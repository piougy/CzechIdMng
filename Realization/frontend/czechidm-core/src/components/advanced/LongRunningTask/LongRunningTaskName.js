import React from 'react';
import PropTypes from 'prop-types';
//
import * as Basic from '../../basic';
import * as Utils from '../../../utils';
import {
  FormAttributeManager
} from '../../../redux';

const formAttributeManager = new FormAttributeManager();

/**
 * Full task name with icon and localization - id form definition is available.
 *
 * @author Radek Tomiška
 * @since 10.4.0
 */
export default class LongRunningTaskName extends Basic.AbstractContextComponent {

  render() {
    const { rendered, showLoading, entity, supportedTasks, showTaskType, className, style, face } = this.props;
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
    const simpleTaskType = Utils.Ui.getSimpleJavaType(entity.taskType);
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
    let _label = simpleTaskType;
    let _icon = 'component:scheduled-task';
    if (_taskType && _taskType.formDefinition) {
      _label = formAttributeManager.getLocalization(_taskType.formDefinition, null, 'label', _label);
      _icon = formAttributeManager.getLocalization(_taskType.formDefinition, null, 'icon', _icon);
    }
    let _textLabel = _label;
    if (_label !== simpleTaskType) {
      // append simple taks type name as new line
      _label = (
        <span>
          <Basic.Icon value={ _icon } style={{ marginRight: 3 }}/>
          {
            showTaskType
            ?
            <strong>
              { _label }
            </strong>
            :
            _label
          }
          {
            !showTaskType
            ||
            <small style={{ display: 'block' }}>
              { `(${ simpleTaskType })` }
            </small>
          }
        </span>
      );
      if (showTaskType) {
        _textLabel += ` (${ simpleTaskType })`;
      }
    }
    //
    if (face === 'text') {
      return _textLabel;
    }
    return (
      <span title={ entity.taskType } className={ className } style={ style }>
        { _label }
      </span>
    );
  }
}

LongRunningTaskName.propTypes = {
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
  /**
   * Shows simple task type, when localization is found.
   */
  showTaskType: PropTypes.bool,
  /**
   * Decorator
   */
  face: PropTypes.oneOf(['text', 'full'])
};
LongRunningTaskName.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  showTaskType: true,
  face: 'full'
};
