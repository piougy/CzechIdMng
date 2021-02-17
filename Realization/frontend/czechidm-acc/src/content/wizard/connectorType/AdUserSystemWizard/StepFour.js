import { Basic, Managers } from 'czechidm-core';
import React from 'react';
import AbstractWizardStep from '../../AbstractWizardStep';

const roleManager = new Managers.RoleManager();
/**
 * Step four of MS AD wizard (subtrees).
 *
 * @author Vít Švanda
 * @since 10.8.0
 */
export default class StepFour extends AbstractWizardStep {

  constructor(props, context) {
    super(props, context);
    const wizardContext = context.wizardContext;
    this.state.pairingSyncSwitch = !props.reopened;
    if (wizardContext.connectorType
      && wizardContext.connectorType.metadata
      && wizardContext.connectorType.metadata.protectedModeSwitch) {
      this.state.protectedModeSwitch = wizardContext.connectorType.metadata.protectedModeSwitch === 'true';
    }
    this.state.newRoleWithSystem = wizardContext.entity ? `${wizardContext.entity.name}-users` : null;
  }

  /**
   * Is call after execution of the step on backend.
   * Good place for set result to the wizard context.
   */
  afterNextAction(wizardContext, json) {
    wizardContext.entity = json._embedded.system;
    wizardContext.mapping = json._embedded.mapping;
    wizardContext.connectorType = json;
  }

  /**
   * Prepare metadata for next action (send to the BE).
   */
  compileMetadata(_connectorType, formData, system) {
    const formPairingSync = this.refs.formPairingSync.getData();
    if (!this.refs.formPairingSync.isFormValid()) {
      return;
    }
    const metadata = _connectorType.metadata;
    metadata.system = system ? system.id : null;
    metadata.newUserContainer = formData.newUserContainer;
    metadata.searchUserContainer = formData.searchUserContainer;
    metadata.deleteUserContainer = formData.deleteUserContainer;
    metadata.protectedModeSwitch = this.state.protectedModeSwitch;
    metadata.domainContainer = formData.domainContainer;
    // formPairingSync
    metadata.pairingSyncSwitch = formPairingSync.pairingSyncSwitch;
    metadata.newRoleWithSystemCode = formPairingSync.newRoleWithSystem;
  }

  _togglePairingSyncSwitch() {
    const value = this.refs.pairingSyncSwitch.getValue();
    this.setState({
      pairingSyncSwitch: !value
    });
  }

  _toggleProtectedModeSwitch(event, value) {
    this.setState({
      protectedModeSwitch: value
    });
  }

  _onChangeRoleWithSystem(value) {
    this.setState({
      newRoleWithSystem: value ? value.baseCode : null,
      roleWithSystem: value
    });
  }

  _onChangeNewRoleWithSystem(event) {
    const baseCode = event.currentTarget.value;
    this.setState({
      newRoleWithSystem: baseCode,
      roleWithSystem: null
    });
  }

  render() {
    const {connectorType, reopened} = this.props;
    const {showLoading,
      pairingSyncSwitch,
      newRoleWithSystem,
      roleWithSystem,
      protectedModeSwitch} = this.state;

    const _connectorType = this.state.connectorType ? this.state.connectorType : connectorType;
    const formData = {};
    if (_connectorType && _connectorType.metadata) {
      const metadata = _connectorType.metadata;
      formData.newUserContainer = metadata.newUserContainer ? metadata.newUserContainer : metadata.userContainer;
      formData.searchUserContainer = metadata.searchUserContainer ? metadata.searchUserContainer : metadata.userContainer;
      formData.deleteUserContainer = metadata.deleteUserContainer ? metadata.deleteUserContainer : metadata.userContainer;
      formData.domainContainer = metadata.domainContainer;
    }
    const formDataPairingSync = {};
    formDataPairingSync.pairingSyncSwitch = pairingSyncSwitch;
    formDataPairingSync.newRoleWithSystem = newRoleWithSystem;
    formDataPairingSync.roleWithSystem = roleWithSystem;

    const locKey = this.getLocKey();
    const finishStepLocKey = 'acc:wizard.create-system.steps.summary';

    return (
      <Basic.Div showLoading={showLoading}>
        <Basic.AbstractForm
          ref="form"
          data={formData}>
          <Basic.TextField
            ref="searchUserContainer"
            label={this.i18n(`${locKey}.searchUserContainer.label`)}
            helpBlock={this.i18n(`${locKey}.searchUserContainer.help`)}
            required
            max={255}/>
          <Basic.TextField
            ref="newUserContainer"
            label={this.i18n(`${locKey}.newUserContainer.label`)}
            helpBlock={this.i18n(`${locKey}.newUserContainer.help`)}
            required
            max={255}/>
          <Basic.Div style={{display: 'flex'}}>
            <Basic.Div style={{flex: 2}}>
              <Basic.ToggleSwitch
                value={protectedModeSwitch}
                label={this.i18n(`${locKey}.protectedModeSwitch.label`)}
                helpBlock={this.i18n(`${locKey}.protectedModeSwitch.help`)}
                onChange={this._toggleProtectedModeSwitch.bind(this)}
              />
            </Basic.Div>
            <Basic.Div style={{flex: 3, marginLeft: 15}}>
              <Basic.TextField
                ref="deleteUserContainer"
                label={this.i18n(`${locKey}.deleteUserContainer.label`)}
                helpBlock={this.i18n(`${locKey}.deleteUserContainer.help`)}
                readOnly={!protectedModeSwitch}
                required
                max={255}/>
            </Basic.Div>
          </Basic.Div>
          <Basic.TextField
            ref="domainContainer"
            label={this.i18n(`${locKey}.domainContainer.label`)}
            helpBlock={this.i18n(`${locKey}.domainContainer.help`)}
            required
            max={255}/>
        </Basic.AbstractForm>
        <Basic.AbstractForm
          ref="formPairingSync"
          data={formDataPairingSync}>
          <Basic.Panel
            rendered={!reopened}
            style={{backgroundColor: '#d9edf7', borderColor: '#bce8f1', color: '#31708f'}}>
            <Basic.PanelBody style={{padding: 10, paddingBottom: 0}}>
              <Basic.ToggleSwitch
                ref="pairingSyncSwitch"
                label={this.i18n(`${locKey}.pairingSyncSwitch.label`)}
                helpBlock={this.i18n(`${locKey}.pairingSyncSwitch.help`)}
                onChange={this._togglePairingSyncSwitch.bind(this)}
              />
              <Basic.Row>
                <Basic.Col lg={ 5 } md={ 5 }>
                  <Basic.TextField
                    ref="newRoleWithSystem"
                    required={!!pairingSyncSwitch}
                    readOnly={!!roleWithSystem}
                    hidden={!pairingSyncSwitch}
                    label={this.i18n(`${locKey}.newRoleWithSystem.label`)}
                    onChange={this._onChangeNewRoleWithSystem.bind(this)}
                    helpBlock={this.i18n(`${finishStepLocKey}.newRoleWithSystem.help`)}/>
                </Basic.Col>
                <Basic.Col lg={ 7 } md={ 7 }>
                  <Basic.SelectBox
                    ref="roleWithSystem"
                    hidden={!pairingSyncSwitch}
                    manager={roleManager}
                    onChange={this._onChangeRoleWithSystem.bind(this)}
                    label={this.i18n(`${finishStepLocKey}.roleWithSystem.label`)}/>
                </Basic.Col>
              </Basic.Row>
            </Basic.PanelBody>
          </Basic.Panel>
        </Basic.AbstractForm>
      </Basic.Div>
    );
  }
}
