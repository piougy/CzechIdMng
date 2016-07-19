

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import DecisionButtons from './DecisionButtons';

class DynamicTaskDetail extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {showLoading: props.showLoading, readOnly: true};
  }

  componentDidMount() {
    const { task} = this.props;
    this.refs.form.setData(task);
    const formDataValues = this._toFormDataValues(task.formData);
    this.refs.formData.setData(formDataValues);
  }

  getContentKey() {
    return 'content.task.instance';
  }

  _toFormDataValues(formDatas) {
    const result = {};
    for (const formData of formDatas) {
      result[formData.id] = formData.value;
    }
    return result;
  }

  _toFormData(formDataValues, formDatas) {
    const result = _.merge({}, formDataValues);
    for (const formData of formDatas) {
      if (!formData.writable && !formData.required) {
        delete result[formData.id];
      }
    }
    return result;
  }

  _validateAndCompleteTask(decision) {
    if (!this.refs.form.isFormValid()) {
      return;
    }
    if (!this.refs.formData.isFormValid()) {
      return;
    }
    if (decision.showWarning) {
      this.refs.confirm.show(this.i18n(decision.warningMessage ? decision.warningMessage : 'completeTaskConfirmDetail'), this.i18n('completeTaskConfirmTitle'))
      .then(() => {
        this.setState({
          showLoading: true
        });
        this._completeTask(decision);
      });
    } else {
      this._completeTask(decision);
    }
  }

  _completeTask(decision) {
    const formDataValues = this.refs.formData.getData();
    const task = this.refs.form.getData();
    const formData = {'decision': decision.id, 'formData': this._toFormData(formDataValues, task.formData)};
    const { taskManager, uiKey } = this.props;
    this.context.store.dispatch(taskManager.completeTask(task, formData, `${uiKey}`, this._afterComplete.bind(this)));
  }

  _afterComplete(task, error) {
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('successComplete', { name: task.name }) });
    this.setState({
      showLoading: false
    });
    this.context.router.goBack();
  }

  _getFormDataComponents(task) {
    if (!task) {
      return null;
    }
    const formDatas = task.formData;
    const formDataComponents = [];
    for (const formData of formDatas) {
      switch (formData.type) {
        case 'textField': {
          formDataComponents.push(
            <Basic.TextField
              key={formData.id}
              ref={formData.id}
              readOnly={!formData.writable}
              required={formData.required}
              tooltip={formData.tooltip}
              placeholder={formData.placeholder}
              label={this.i18n(formData.name)}/>
          );
          break;
        }
        case 'date': {
          formDataComponents.push(
            <Basic.DateTimePicker
              key={formData.id}
              ref={formData.id}
              mode="date"
              readOnly={!formData.writable}
              required={formData.required}
              tooltip={formData.tooltip}
              placeholder={formData.placeholder}
              label={this.i18n(formData.name)}/>
          );
          break;
        }
        case 'checkbox': {
          formDataComponents.push(
            <Basic.Checkbox
              key={formData.id}
              ref={formData.id}
              readOnly={!formData.writable}
              required={formData.required}
              tooltip={formData.tooltip}
              placeholder={formData.placeholder}
              label={this.i18n(formData.name)}/>
          );
          break;
        }
        case 'textArea': {
          formDataComponents.push(
            <Basic.TextArea
              key={formData.id}
              ref={formData.id}
              readOnly={!formData.writable}
              required={formData.required}
              tooltip={formData.tooltip}
              placeholder={formData.placeholder}
              label={this.i18n(formData.name)}/>
          );
          break;
        }
        default: {
          formDataComponents.push(
            <Basic.TextField
              key={formData.id}
              ref={formData.id}
              readOnly={!formData.writable}
              required={formData.required}
              tooltip={formData.tooltip}
              placeholder={formData.placeholder}
              label={this.i18n(formData.name)}/>
          );
        }
      }
    }
    return formDataComponents;
  }

  render() {
    const {task, taskManager} = this.props;
    const { showLoading} = this.state;
    const showLoadingInternal = task ? showLoading : true;

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm"/>
        <Basic.PageHeader>{task.name}
          <small> {this.i18n('header')}</small>
        </Basic.PageHeader>
        <Basic.Panel showLoading = {showLoadingInternal}>
          <Basic.PanelHeader text={<span>{taskManager.getNiceLabel(task)} <small>this.i18n('taskDetail')</small></span>} className="hidden">
          </Basic.PanelHeader>
          <Basic.AbstractForm ref="form">
            <Basic.TextField ref="taskName" readOnly label={this.i18n('name')}/>
            <Basic.TextField ref="taskDescription" readOnly label={this.i18n('description')}/>
            <Basic.LabelWrapper readOnly ref="applicant" label={this.i18n('applicant')} componentSpan="col-sm-5">
              <Advanced.IdentityInfo username={task.applicant} showLoading={!task} className="no-margin"/>
            </Basic.LabelWrapper>
            <Basic.DateTimePicker ref="taskCreated" readOnly label={this.i18n('createdDate')}/>
          </Basic.AbstractForm>
          <Basic.AbstractForm ref="formData">
            {this._getFormDataComponents(task)}
          </Basic.AbstractForm>
          <Basic.PanelFooter>
            <DecisionButtons task={task} onClick={this._validateAndCompleteTask.bind(this)}/>
          </Basic.PanelFooter>
        </Basic.Panel>
      </div>
    );
  }
}

DynamicTaskDetail.propTypes = {
  task: PropTypes.object,
  readOnly: PropTypes.bool,
  taskManager: PropTypes.object.isRequired
};

DynamicTaskDetail.defaultProps = {
  task: null,
  readOnly: false
};

export default DynamicTaskDetail;
