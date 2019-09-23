import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import _ from 'lodash';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import DecisionButtons from '../DecisionButtons';
import DynamicTaskDetail from '../DynamicTaskDetail';
import RoleConceptDetail from '../../requestrole/RoleConceptDetail';
import { ConceptRoleRequestManager } from '../../../redux';

const conceptRoleRequestManager = new ConceptRoleRequestManager();

/**
 * Custom task detail designed for use with RoleConceptDetail.
 * Extended from DynamicTaskDetail (it is standard task detail renderer)
 *
 * @author Vít Švanda
 */
class DynamicTaskRoleConceptDetail extends DynamicTaskDetail {

  getContentKey() {
    return 'content.task.instance';
  }

  componentDidMount() {
    super.componentDidMount();
    this._initComponent(this.props);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps && (
      JSON.stringify(nextProps.task) !== JSON.stringify(this.props.task)
    )) {
      this._initComponent(nextProps);
    }
  }

  /**
   * Method for init component from didMount method and from willReceiveProps method
   * @param  {properties of component} props For didmount call is this.props for call from willReceiveProps is nextProps.
   */
  _initComponent(props) {
    const { task} = props;
    const _entityId = task && task.variables && task.variables.conceptRole ? task.variables.conceptRole.id : null;
    if (_entityId) {
      this.context.store.dispatch(conceptRoleRequestManager.fetchEntity(_entityId));
    }
  }

  _completeTask(decision) {
    const formDataValues = this.refs.formData.getData();
    const task = this.refs.form.getData();
    const formDataConverted = this._toFormData(formDataValues, task.formData);
    this.setState({
      showLoading: true
    });
    const formData = {
      decision: decision.id,
      formData: formDataConverted};
    const { taskManager} = this.props;
    this.context.store.dispatch(taskManager.completeTask(task, formData, this.props.uiKey, this._afterComplete.bind(this)));
  }


  _updateConcept(data, type, formInstance) {
    this.setState({showLoading: true});
    const concept = {
      id: data.id,
      operation: type,
      roleRequest: data.roleRequest,
      identityContract: data.identityContract.id,
      role: data.role,
      identityRole: data.identityRole,
      validFrom: data.validFrom,
      validTill: data.validTill,
      state: data.state,
      wfProcessId: data.wfProcessId,
      log: data.log,
      _eav: [formInstance]
    };
    this.context.store.dispatch(conceptRoleRequestManager.updateEntity(concept, null, (updatedEntity, error) => {
      if (!error) {
        this._initComponent(this.props);
        this.setState({showLoading: false});
      } else {
        this.setState({showLoading: false});
        this.addError(error);
      }
    }));
  }

  /**
   * Save added or changed entities to arrays and recompile concept data.
   */
  _saveConcept(event) {
    if (event) {
      event.preventDefault();
    }

    const form = this.refs.roleConceptDetail.getWrappedInstance().getForm();
    const eavForm = this.refs.roleConceptDetail.getWrappedInstance().getEavForm();
    if (!form.isFormValid()) {
      return;
    }
    if (eavForm && !eavForm.isValid()) {
      return;
    }

    const entity = form.getData();
    let eavValues = null;
    if (eavForm) {
      eavValues = {values: eavForm.getValues()};
    }
    this._updateConcept(entity, entity.operation, eavValues);
  }

  _goBack() {
    const { task } = this.props;
    const { canGoBack } = this.state;
    if (canGoBack) {
      this.context.router.goBack();
    } else {
      // transmition to /task, history doesnt exist, for now transmition to identity task
      this.context.router.push(`tasks/identity/${task.variables.implementerIdentifier}`);
    }
  }

  render() {
    const {task, canExecute, taskManager, _entity} = this.props;
    const { showLoading} = this.state;
    const showLoadingInternal = task && _entity ? showLoading : true;
    const formDataValues = this._toFormDataValues(task.formData);
    const taskName = taskManager.localize(task, 'name');
    const entity = _entity ? _.merge({}, _entity) : null;
    if (_entity && _entity.identityContract && _entity._embedded && _entity._embedded.identityContract) {
      entity.identityContract = _entity._embedded.identityContract;
    }
    if (_entity && _entity.role && _entity._embedded && _entity._embedded.role) {
      entity.role = _entity._embedded.role;
    }

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm"/>

        <Basic.PageHeader>
          {taskName}
          <small>
            {' '}
            {this.i18n('header')}
          </small>
        </Basic.PageHeader>

        <Basic.Panel showLoading={showLoadingInternal}>
          <Basic.AbstractForm className="panel-body" ref="form" data={task}>
            {this._getTaskInfo(task)}
            {this._getApplicantAndRequester(task)}
            <Basic.DateTimePicker ref="taskCreated" readOnly label={this.i18n('createdDate')}/>
          </Basic.AbstractForm>
          <Basic.AbstractForm ref="formData" data={formDataValues} style={{ padding: '15px 15px 0px 15px' }}>
            {this._getFormDataComponents(task)}
          </Basic.AbstractForm>
          <Basic.PanelFooter>
            <DecisionButtons
              task={task}
              onClick={this._validateAndCompleteTask.bind(this)}
              showBackButton
              readOnly={!canExecute} />
          </Basic.PanelFooter>
        </Basic.Panel>
        <Basic.Panel showLoading={showLoadingInternal}>
          <RoleConceptDetail
            ref="roleConceptDetail"
            identityUsername={task.applicant}
            showLoading={showLoadingInternal}
            style={{ padding: '15px 15px 0px 15px' }}
            readOnly={!canExecute}
            entity={entity}
            isEdit={canExecute}
            multiAdd={false}
          />
          <Basic.PanelFooter>
            <Basic.Button
              type="button"
              level="link"
              onClick={this._goBack.bind(this)}
              showLoading={showLoading}>
              {this.i18n('button.back')}
            </Basic.Button>
            <Basic.Button
              level="success"
              onClick={this._saveConcept.bind(this)}
              showLoading={ showLoading }
              showLoadingIcon
              rendered={canExecute}>
              { this.i18n('button.save') }
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </div>
    );
  }
}

DynamicTaskRoleConceptDetail.propTypes = {
  task: PropTypes.object,
  readOnly: PropTypes.bool,
  taskManager: PropTypes.object.isRequired,
  canExecute: PropTypes.bool
};

DynamicTaskRoleConceptDetail.defaultProps = {
  task: null,
  readOnly: false,
  canExecute: true
};
function select(state, component) {
  const task = component.task;
  const _entityId = task && task.variables && task.variables.conceptRole ? task.variables.conceptRole.id : null;
  const entity = conceptRoleRequestManager.getEntity(state, _entityId);
  if (task && entity) {
    return {
      _showLoading: false,
      _entity: entity
    };
  }
  return {
    _showLoading: true
  };
}

export default connect(select, null, null, { withRef: true })(DynamicTaskRoleConceptDetail);
