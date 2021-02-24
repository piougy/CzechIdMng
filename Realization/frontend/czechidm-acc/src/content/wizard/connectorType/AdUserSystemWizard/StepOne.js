import { Basic } from 'czechidm-core';
import React from 'react';
import Joi from 'joi';
import AbstractWizardStep from '../../AbstractWizardStep';


/**
 * First step of MS AD connector.
 *
 * @author Vít Švanda
 * @since 10.8.0
 */
export default class StepOne extends AbstractWizardStep {

  constructor(props, context) {
    super(props, context);
    const wizardContext = context.wizardContext;
    this.state.sslSwitch = true;
    const metadata = this.state.connectorType.metadata;
    if (metadata && metadata.sslSwitch !== undefined) {
      this.state.sslSwitch = metadata.sslSwitch === 'true';
    }
    // If context contains connectorType, then we will used it.
    if (wizardContext && wizardContext.connectorType) {
      if (!wizardContext.connectorType.reopened) {
        //
      }
    }
  }

  /**
   * Prepare metadata for next action (send to the BE).
   */
  compileMetadata(_connectorType, formData, system) {
    const metadata = _connectorType.metadata;
    metadata.system = system ? system.id : null;
    metadata.name = formData.name;
    metadata.port = formData.port;
    metadata.host = formData.host;
    metadata.user = formData.user;
    metadata.password = formData.password;
    metadata.database = formData.database;
    metadata.table = formData.table;
    metadata.keyColumn = formData.keyColumn;
    metadata.sslSwitch = formData.sslSwitch;
  }

  /**
   * Is call after execution of the step on backend.
   * Good place for set result to the wizard context.
   */
  afterNextAction(wizardContext, json) {
    wizardContext.entity = json._embedded.system;
    wizardContext.connectorType = json;
  }

  _toggleSslSwitch() {
    const {connectorType} = this.props;
    const value = this.refs.sslSwitch.getValue();
    const _connectorType = this.state.connectorType ? this.state.connectorType : connectorType;
    if (_connectorType && _connectorType.metadata) {
      _connectorType.metadata.port = !value ? '636' : '389';
    }
    this.setState({
      sslSwitch: !value
    });
  }

  render() {
    const {connectorType} = this.props;
    const {showLoading, sslSwitch} = this.state;

    const _connectorType = this.state.connectorType ? this.state.connectorType : connectorType;
    const formData = {};
    if (_connectorType && _connectorType.metadata) {
      const metadata = _connectorType.metadata;
      formData.name = metadata.name;
      formData.port = metadata.port;
      formData.host = metadata.host;
      formData.user = metadata.user;
      formData.sslSwitch = sslSwitch;
      if (_connectorType.reopened) {
        // We expecting the password was already filled for reopened system.
        formData.password = '********';
      }

      // TODO delete it:
      formData.host = 'adradic1.piskoviste.bcv';
      formData.user = 'CN=Administrator,CN=Users,DC=piskoviste,DC=bcv';
      formData.password = 'Demo123456';
    }
    const locKey = this.getLocKey();

    return (
      <Basic.Div showLoading={showLoading}>
        <Basic.AbstractForm
          ref="form"
          onSubmit={(event) => {
            this.wizardNext(event);
          }}
          data={formData}>
          <Basic.TextField
            ref="name"
            label={this.i18n(`${locKey}.systemName`)}
            required
            max={255}/>
          <Basic.ToggleSwitch
            ref="sslSwitch"
            label={this.i18n(`${locKey}.sslSwitch.label`)}
            helpBlock={this.i18n(`${locKey}.sslSwitch.help`)}
            onChange={this._toggleSslSwitch.bind(this)}
          />
          <Basic.Alert
            title={this.i18n(`${locKey}.sslOffAlert.title`)}
            text={this.i18n(`${locKey}.sslOffAlert.text`)}
            showHtmlText
            rendered={!formData.sslSwitch}
            level="warning"
          />
          <Basic.Div style={{display: 'flex'}}>
            <Basic.Div style={{flex: 3}}>
              <Basic.TextField
                ref="host"
                label={this.i18n(`${locKey}.host.label`)}
                helpBlock={this.i18n(`${locKey}.host.help`)}
                required
                max={128}/>
            </Basic.Div>
            <Basic.Div style={{flex: 1, marginLeft: 15}}>
              <Basic.TextField
                ref="port"
                validation={Joi.number().integer().min(0).max(65535)}
                label={this.i18n(`${locKey}.port.label`)}
                helpBlock={this.i18n(`${locKey}.port.help`)}
                required/>
            </Basic.Div>
          </Basic.Div>
          <Basic.Div style={{display: 'flex'}}>
            <Basic.Div style={{flex: 1, marginRight: 15}}>
              <Basic.TextField
                ref="user"
                label={this.i18n(`${locKey}.user.label`)}
                helpBlock={this.i18n(`${locKey}.user.help`)}
                required
                max={128}/>
            </Basic.Div>
            <Basic.Div style={{flex: 1}}>
              <Basic.TextField
                ref="password"
                pwdAutocomplete={false}
                required={_connectorType ? !_connectorType.reopened : true}
                type="password"
                label={this.i18n(`${locKey}.password.label`)}
                max={255}/>
            </Basic.Div>
          </Basic.Div>
        </Basic.AbstractForm>
      </Basic.Div>
    );
  }
}
