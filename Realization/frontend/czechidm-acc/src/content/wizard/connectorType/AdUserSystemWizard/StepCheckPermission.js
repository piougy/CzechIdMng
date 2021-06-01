import { Basic } from 'czechidm-core';
import React from 'react';
import _ from 'lodash';
import AbstractWizardStep from '../../AbstractWizardStep';


/**
 * Step three of MS AD wizard (permissions check).
 *
 * @author Vít Švanda
 * @since 10.8.0
 */
export default class StepCheckPermission extends AbstractWizardStep {

  /**
   * Is call after execution of the step on backend.
   * Good place for set result to the wizard context.
   */
  afterNextAction(wizardContext, json) {
    wizardContext.entity = json._embedded.system;
    wizardContext.connectorType = json;
  }

  onCreateTestUser(event) {
    if (event) {
      event.preventDefault();
    }
    const {connectorType} = this.props;
    const formData = this.refs.form.getData();
    if (!this.refs.form.isFormValid()) {
      return;
    }

    const _connectorType = _.merge({}, this.state.connectorType ? this.state.connectorType : connectorType);
    if (_connectorType && _connectorType.metadata) {
      const metadata = _connectorType.metadata;
      _connectorType.wizardStepName = 'stepCreateUserTest';
      metadata.userContainer = formData.userContainer;
      metadata.testUserName = formData.testUserName;
      metadata.system = _connectorType._embedded.system.id;
    }
    this.setState({
      showLoadingTestUser: true
    }, () => {
      this.getSystemManager().getService().executeConnectorType(_connectorType)
        .then((json) => {
          json.wizardStepName = this.getStepId();
          json.metadata.testUserDeleted = false;
          json.metadata.testUserCreated = true;
          json.metadata.testAssignedGroup = false;

          const wizardContext = this.context.wizardContext;
          wizardContext.connectorType = json;
          this.setState({
            connectorType: json,
            showLoadingTestUser: false
          });
          this.addMessage({
            level: 'success',
            message: this.i18n(`${this.getLocKey()}.testCreatedUserDN.text`, {dn: json.metadata.testCreatedUserDN})
          });
        })
        .catch(ex => {
          this.setState({
            showLoadingTestUser: false
          });
          this.addError(ex);
        });
    });
  }

  onDeleteTestUser(event) {
    if (event) {
      event.preventDefault();
    }
    const {connectorType} = this.props;

    const _connectorType = _.merge({}, this.state.connectorType ? this.state.connectorType : connectorType);
    if (_connectorType && _connectorType.metadata) {
      _connectorType.wizardStepName = 'stepDeleteUserTest';
    }
    this.setState({
      showLoadingTestUser: true
    }, () => {
      this.getSystemManager().getService().executeConnectorType(_connectorType)
        .then((json) => {
          json.wizardStepName = this.getStepId();
          json.metadata.testUserDeleted = true;
          const testCreatedUserDN = json.metadata.testCreatedUserDN;
          json.metadata.testCreatedUserDN = null;
          const wizardContext = this.context.wizardContext;
          wizardContext.connectorType = json;
          this.setState({
            connectorType: json,
            showLoadingTestUser: false
          });
          this.addMessage({
            level: 'success',
            message: this.i18n(`${this.getLocKey()}.testDeletedUserDN.text`, {dn: testCreatedUserDN})
          });
        })
        .catch(ex => {
          this.setState({
            showLoadingTestUser: false
          });
          this.addError(ex);
        });
    });
  }

  onTestAssignGroup(event) {
    if (event) {
      event.preventDefault();
    }
    const {connectorType} = this.props;
    const formData = this.refs.form.getData();
    if (!this.refs.form.isFormValid()) {
      return;
    }

    const _connectorType = _.merge({}, this.state.connectorType ? this.state.connectorType : connectorType);
    if (_connectorType && _connectorType.metadata) {
      const metadata = _connectorType.metadata;
      _connectorType.wizardStepName = 'stepAssignToGroupTest';
    }
    this.setState({
      showLoadingTestUser: true
    }, () => {
      this.getSystemManager().getService().executeConnectorType(_connectorType)
        .then((json) => {
          json.wizardStepName = this.getStepId();
          json.metadata.testAssignedGroup = true;
          json.metadata.testUserDeleted = false;

          const wizardContext = this.context.wizardContext;
          wizardContext.connectorType = json;
          this.setState({
            connectorType: json,
            showLoadingTestUser: false
          });
          this.addMessage({
            level: 'success',
            message: this.i18n(`${this.getLocKey()}.testAssignedGroupDN.text`)
          });
        })
        .catch(ex => {
          this.setState({
            showLoadingTestUser: false
          });
          this.addError(ex);
        });
    });
  }

  /**
   * Prepare metadata for next action (send to the BE).
   */
  compileMetadata(_connectorType, formData, system) {
    const metadata = _connectorType.metadata;
    metadata.system = system ? system.id : null;
  }

  /**
   * Is call after execution of the step on backend.
   * Good place for set result to the wizard context.
   */
  afterNextAction(wizardContext, json) {
    wizardContext.entity = json._embedded.system;
    wizardContext.connectorType = json;
  }

  render() {
    const {connectorType} = this.props;
    const {showLoading, showLoadingTestUser} = this.state;

    const _connectorType = this.state.connectorType ? this.state.connectorType : connectorType;
    const formData = {};
    if (_connectorType && _connectorType.metadata) {
      const metadata = _connectorType.metadata;
      formData.userContainer = metadata.userContainer;
      formData.testUserName = metadata.testUserName;
      formData.testGroup = metadata.testGroup;
      formData.testCreatedUserDN = metadata.testCreatedUserDN;
      formData.testUserDeleted = `${metadata.testUserDeleted}` === 'true';
      formData.testAssignedGroup = `${metadata.testAssignedGroup}` === 'true';
      formData.testUserCreated = `${metadata.testUserCreated}` === 'true';
    }
    const locKey = this.getLocKey();

    return (
      <Basic.Div showLoading={showLoading}>
        <Basic.AbstractForm
          ref="form"
          data={formData}>
          <Basic.TextField
            ref="testUserName"
            label={this.i18n(`${locKey}.testUserName`)}
            readOnly={!!formData.testCreatedUserDN && !formData.testUserDeleted}
            required
            max={255}/>
          <Basic.TextField
            ref="userContainer"
            label={this.i18n(`${locKey}.userContainer.label`)}
            helpBlock={this.i18n(`${locKey}.userContainer.help`)}
            required
            readOnly={!!formData.testCreatedUserDN && !formData.testUserDeleted}
            max={255}/>
          <Basic.TextField
            ref="testGroup"
            label={this.i18n(`${locKey}.testGroup.label`)}
            helpBlock={this.i18n(`${locKey}.testGroup.help`)}
            required
            readOnly
            max={255}/>
          <Basic.Alert
            ref="testLabel"
            level="info"
            title={this.i18n(`${locKey}.testLabel.label`)}
            text={this.i18n(`${locKey}.testLabel.help`)}/>
          <Basic.Div style={{display: 'flex'}}>
            <Basic.Div style={{flex: 1}}>
              <Basic.Div style={{display: 'flex'}}>
                <Basic.Div style={{flex: 1, minWidth: 150, marginTop: 8}}>
                  <label className="control-label">
                    {this.i18n(`${locKey}.testCreateUserBtn.text`)}:
                  </label>
                </Basic.Div>
                <Basic.Div style={{flex: 1}}>
                  <Basic.Button
                    ref="testCreateUser"
                    level="success"
                    showLoading={ !!showLoadingTestUser }
                    showLoadingIcon
                    disabled={!!formData.testCreatedUserDN && !formData.testUserDeleted}
                    icon="fa:plus"
                    onClick={ this.onCreateTestUser.bind(this) }>
                    {this.i18n(`${locKey}.btnTest`)}
                  </Basic.Button>
                </Basic.Div>
                <Basic.Div style={{flex: 10, marginLeft: 15}}>
                  <Basic.Icon
                    level="success"
                    iconSize="sm"
                    style={formData.testUserCreated ? null : {color: 'lightgray'}}
                    icon="fa:check"/>
                </Basic.Div>
              </Basic.Div>
              <Basic.Div style={{marginTop: 15, display: 'flex'}}>
                <Basic.Div style={{flex: 1, minWidth: 150, marginTop: 6}}>
                  <label className="control-label">
                    {this.i18n(`${locKey}.testAssignGroupBtn.text`)}:
                  </label>
                </Basic.Div>
                <Basic.Div style={{flex: 1}}>
                  <Basic.Button
                    ref="testAssignGroup"
                    level="info"
                    showLoading={ !!showLoadingTestUser }
                    showLoadingIcon
                    disabled={!formData.testCreatedUserDN || !!formData.testAssignedGroup}
                    icon="fa:plus"
                    onClick={ this.onTestAssignGroup.bind(this) }>
                    {this.i18n(`${locKey}.btnTest`)}
                  </Basic.Button>
                </Basic.Div>
                <Basic.Div style={{flex: 10, marginLeft: 15}}>
                  <Basic.Icon
                    level="success"
                    iconSize="sm"
                    style={formData.testAssignedGroup ? null : {color: 'lightgray'}}
                    icon="fa:check"/>
                </Basic.Div>
              </Basic.Div>
              <Basic.Div style={{marginTop: 15, display: 'flex'}}>
                <Basic.Div style={{flex: 1, minWidth: 150, marginTop: 6}}>
                  <label className="control-label">
                    {this.i18n(`${locKey}.testDeleteUserBtn.text`)}:
                  </label>
                </Basic.Div>
                <Basic.Div style={{flex: 1}}>
                  <Basic.Button
                    ref="testDeleteUser"
                    level="danger"
                    showLoading={ !!showLoadingTestUser }
                    showLoadingIcon
                    disabled={!formData.testCreatedUserDN || formData.testUserDeleted}
                    icon="fa:minus"
                    onClick={ this.onDeleteTestUser.bind(this) }>
                    {this.i18n(`${locKey}.btnTest`)}
                  </Basic.Button>
                </Basic.Div>
                <Basic.Div style={{flex: 10, marginLeft: 15}}>
                  <Basic.Icon
                    level="success"
                    iconSize="sm"
                    style={formData.testUserDeleted ? null : {color: 'lightgray'}}
                    icon="fa:check"/>
                </Basic.Div>
              </Basic.Div>
            </Basic.Div>
            <Basic.Div style={{flex: 2}}>
              <Basic.Alert
                style={{marginLeft: 15, marginTop: 30}}
                text={this.i18n(`${locKey}.permissionChecked.text`)}
                showHtmlText
                rendered={!!formData.testUserCreated && !!formData.testUserDeleted && !!formData.testAssignedGroup}
                level="success"
              />
            </Basic.Div>
          </Basic.Div>
        </Basic.AbstractForm>
      </Basic.Div>
    );
  }
}
