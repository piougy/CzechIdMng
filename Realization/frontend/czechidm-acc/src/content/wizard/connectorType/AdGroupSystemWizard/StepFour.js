import {Basic, Managers} from 'czechidm-core';
import React from 'react';
import AbstractWizardStep from '../../AbstractWizardStep';

/**
 * Step four of MS AD group wizard (group container).
 *
 * @author Vít Švanda
 * @since 11.1.0
 */
export default class StepFour extends AbstractWizardStep {

  constructor(props, context) {
    super(props, context);
    const wizardContext = context.wizardContext;
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
    const metadata = _connectorType.metadata;
    metadata.system = system ? system.id : null;
    metadata.groupContainer = formData.groupContainer;
  }

  _toggleSwitch(key) {
    const state = {};
    state[key] = !this.state[key];

    this.setState(state);
  }

  render() {
    const {connectorType} = this.props;
    const {
      showLoading,
      membershipSwitch,
      assignRoleSwitch,
      assignCatalogueSwitch,
      removeCatalogueRoleSwitch,
      forwardAcmSwitch,
      skipValueIfExcludedSwitch
    } = this.state;

    const _connectorType = this.state.connectorType ? this.state.connectorType : connectorType;
    const formData = {};
    if (_connectorType && _connectorType.metadata) {
      const metadata = _connectorType.metadata;
      formData.groupContainer = metadata.groupContainer ? metadata.groupContainer : null;
    }

    const locKey = this.getLocKey();
    const roleSyncLocKey = 'acc:content.system.systemSynchronizationConfigDetail.roleConfigDetail';

    return (
      <Basic.Div showLoading={showLoading}>
        <Basic.AbstractForm
          ref="form"
          onSubmit={(event) => {
            this.wizardNext(event);
          }}
          data={formData}>
          <Basic.TextField
            ref="groupContainer"
            label={this.i18n(`${locKey}.groupContainer.label`)}
            helpBlock={this.i18n(`${locKey}.groupContainer.help`)}
            required
            max={255}/>
          <Basic.ToggleSwitch
            ref="membershipSwitch"
            onChange={this._toggleSwitch.bind(this, 'membershipSwitch')}
            label={this.i18n(`${roleSyncLocKey}.membershipSwitch.label`)}
            helpBlock={this.i18n(`${roleSyncLocKey}.membershipSwitch.helpBlock`)}/>
          <Basic.Div style={{display: 'flex', justifyContent: 'start'}}>
            <Basic.Div style={{flex: 1}}>
              <Basic.ToggleSwitch
                ref="assignCatalogueSwitch"
                onChange={this._toggleSwitch.bind(this, 'assignCatalogueSwitch')}
                label={this.i18n(`${roleSyncLocKey}.assignCatalogueSwitch.label`)}
                helpBlock={this.i18n(`${roleSyncLocKey}.assignCatalogueSwitch.helpBlock`)}/>
            </Basic.Div>
            <Basic.Div style={{flex: 1}}>
              <Basic.ToggleSwitch
                level="danger"
                style={{marginLeft: 15}}
                ref="removeCatalogueRoleSwitch"
                onChange={this._toggleSwitch.bind(this, 'removeCatalogueRoleSwitch')}
                readOnly={!assignCatalogueSwitch}
                label={this.i18n(`${roleSyncLocKey}.removeCatalogueRoleSwitch.label`)}
                helpBlock={this.i18n(`${roleSyncLocKey}.removeCatalogueRoleSwitch.helpBlock`)}/>
            </Basic.Div>
          </Basic.Div>
          <Basic.Div style={{display: 'flex', justifyContent: 'start'}}>
            <Basic.Div style={{flex: 1}}>
              <Basic.ToggleSwitch
                ref="assignRoleSwitch"
                onChange={this._toggleSwitch.bind(this, 'assignRoleSwitch')}
                label={this.i18n(`${roleSyncLocKey}.assignRoleSwitch.label`)}
                helpBlock={this.i18n(`${roleSyncLocKey}.assignRoleSwitch.helpBlock`)}/>
            </Basic.Div>
            <Basic.Div style={{flex: 1}}>
              <Basic.ToggleSwitch
                level="danger"
                style={{marginLeft: 15}}
                ref="assignRoleRemoveSwitch"
                onChange={this._toggleSwitch.bind(this, 'assignRoleRemoveSwitch')}
                readOnly={!assignRoleSwitch}
                label={this.i18n(`${roleSyncLocKey}.assignRoleRemoveSwitch.label`)}
                helpBlock={this.i18n(`${roleSyncLocKey}.assignRoleRemoveSwitch.helpBlock`)}/>
            </Basic.Div>
          </Basic.Div>
          <Basic.Alert
            title={this.i18n(`acc:content.system.systemSynchronizationConfigDetail.roleConfigDetail.assignRoleAndDiffSyncWarning.title`)}
            text={this.i18n(`acc:content.system.systemSynchronizationConfigDetail.roleConfigDetail.assignRoleAndDiffSyncWarning.text`)}
            showHtmlText
            rendered={!!assignRoleSwitch}
            level="warning"/>
        </Basic.AbstractForm>
      </Basic.Div>
    );
  }
}
