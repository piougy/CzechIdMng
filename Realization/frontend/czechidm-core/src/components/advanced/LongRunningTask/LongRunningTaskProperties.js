import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import Immutable from 'immutable';
//
import * as Basic from '../../basic';
import * as Domain from '../../../domain';
import * as Utils from '../../../utils';
import EavForm from '../Form/EavForm';

/**
 * Long running task properites. Render properties with optionable form definition.
 *
 * @author Radek Tomiška
 * @since 10.4.0
 */
export default class LongRunningTaskProperties extends Basic.AbstractContextComponent {

  _getProperties(entity) {
    if (!entity) {
      return null;
    }
    return entity.taskProperties || entity.parameters;
  }

  render() {
    const { rendered, showLoading, entity, supportedTasks, condensed } = this.props;
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
    let _taskType = null;
    const entityProperties = this._getProperties(entity);
    let formInstance = new Domain.FormInstance({});
    if (supportedTasks && supportedTasks.has(entity.taskType)) {
      _taskType = supportedTasks.get(entity.taskType);
      //
      if (_taskType.formDefinition) {
        formInstance = new Domain.FormInstance(_taskType.formDefinition).setProperties(entityProperties);
      }
    } else if (entityProperties) {
      const bulkAction = entityProperties['core:bulkAction'];
      // try to find form attributes from form definition
      if (bulkAction) {
        formInstance = new Domain
          .FormInstance({
            code: bulkAction.name,
            module: bulkAction.module,
            type: 'bulk-action',
            formAttributes: bulkAction.formAttributes
          })
          .setProperties(entityProperties);
      }
    }
    // form definition is found and instance is inited
    if (formInstance) {
      return (
        <EavForm
          ref="formInstance"
          formInstance={ formInstance }
          useDefaultValue={ false }
          readOnly
          condensed={ condensed }
          showAttributes={ new Immutable.OrderedMap() }/>
      );
    }
    //
    return [..._.keys(entityProperties).map(propertyName => {
      if (Utils.Ui.isEmpty(entityProperties[propertyName])) {
        return null;
      }
      if (propertyName === 'core:transactionContext') {
        // FIXME: transaction context info
        return null;
      }
      if (propertyName === 'core:bulkAction') {
        return null;
      }
      return (
        <Basic.Div>
          { propertyName }: { Utils.Ui.toStringValue(entityProperties[propertyName]) }
        </Basic.Div>
      );
    }).values()];
  }
}

LongRunningTaskProperties.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * LRT or sheduled task (entity taskType and with parameters or kask properties).
   *
   * @type {IdmLongRunningTaskDto} or supported (scheduled) task
   */
  entity: PropTypes.object,
  /**
   * Supported schedulable long running task.
   */
  supportedTasks: PropTypes.arrayOf(PropTypes.object),
  /**
   * Condensed (shorten) form properties - usageble in tables.
   */
  condensed: PropTypes.bool
};
LongRunningTaskProperties.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  condensed: false
};
