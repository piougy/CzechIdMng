import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import { WorkflowHistoricTaskInstanceManager } from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';
import DateValue from '../DateValue/DateValue';
import IdentityInfo from '../IdentityInfo/IdentityInfo';
import WorkflowProcessInfo from '../WorkflowProcessInfo/WorkflowProcessInfo';

const manager = new WorkflowHistoricTaskInstanceManager();


/**
 * Component for rendering task historic info
 *
 * @author Vít Švanda
 */
export class TaskHistoricInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  getNiceLabel() {
    const _entity = this.getEntity();
    const { showIdentity } = this.props;
    //
    return this.getManager().getNiceLabel(_entity, showIdentity);
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'check';
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('content.task.historicInstance.name');
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
  getPopoverContent(task) {
    const result = [
      {
        label: this.i18n('content.task.historicInstance.assignee'),
        value: (<IdentityInfo entityIdentifier={task.taskAssignee} showLoading={!task} className="no-margin" face="popover"/>)
      },
      {
        label: this.i18n('content.task.historicInstance.endTime'),
        value: (<DateValue value={task.endTime} showTime/>)
      }
    ];
    // Show task's end decision.
    // Beware, the decision definition is get from task variables. Can be slow and haven't exist in every situations!
    const decision = task.completeTaskDecision;
    if (decision) {
      const decisionDefinition = task.variables ? task.variables[decision] : null;
      let level = 'info';
      if (decisionDefinition) {
        // Decision definition is object in string, we need to convert it.
        level = JSON.parse(decisionDefinition).level;
      }
      result.push({
        label: this.i18n('content.task.historicInstance.result'),
        value: (<Basic.Label level={level} text={this.i18n(`wf.decision.${decision}.resultLabel`, decision)}/>)
      });
    }
    result.push(
      {
        label: this.i18n('content.task.historicInstance.process'),
        value: (<WorkflowProcessInfo maxLength={35} entityIdentifier={task.processInstanceId}/>)
      }
    );
    return result;
  }
}

TaskHistoricInfo.propTypes = {
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
   * Show contract's identity
   */
  showIdentity: PropTypes.bool,
  //
  _showLoading: PropTypes.bool,
  _permissions: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.arrayOf(PropTypes.string)
  ])
};
TaskHistoricInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'full',
  _showLoading: true,
  showIdentity: true,
};

function select(state, component) {
  const entity = manager.getEntity(state, component.entityIdentifier);
  return {
    _entity: entity,
    _showLoading: manager.isShowLoading(state, null, component.entityIdentifier),
    _permissions: manager.getPermissions(state, null, component.entityIdentifier)
  };
}
export default connect(select)(TaskHistoricInfo);
