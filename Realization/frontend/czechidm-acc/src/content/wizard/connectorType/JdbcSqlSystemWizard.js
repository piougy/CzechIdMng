import { Basic } from 'czechidm-core';
import React from 'react';
import Joi from 'joi';
import DefaultSystemWizard from './DefaultSystemWizard';
import AbstractWizardStep from '../AbstractWizardStep';

/**
 * Wizard for create a system with JDBC connector.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
export default class JdbcSqlSystemWizard extends DefaultSystemWizard {

  constructor(props, context) {
    super(props, context);
    this.state = {showWizard: false};
    this.wizardContext = {};
  }

  getWizardId() {
    return 'jdbc-connector-type';
  }

  getModule() {
    return 'acc';
  }

  getWizardSteps(props, context) {
    const steps = super.getWizardSteps(props, context);
    const stepOneId = 'jdbcStepOne';
    // Replace first step.
    steps[0] = {
      id: stepOneId,
      getComponent: () => {
        return (
          <JdbcStepOne
            match={this.props.match}
            wizardStepId={stepOneId}
            connectorType={this.props.connectorType}
            baseLocKey={this.getBaseLocKey()}
          />
        );
      }
    };
    // Remove schema step.
    steps.splice(1, 2);
    return [...steps];
  }
}

/**
 * First step of JDBC connector - Creates a system, generate schema.
 */
class JdbcStepOne extends AbstractWizardStep {

  constructor(props, context) {
    super(props, context);
    const wizardContext = context.wizardContext;
    // If context contains connectorType, then we will used it.
    if (wizardContext && wizardContext.connectorType) {
      if (!wizardContext.connectorType.reopened) {
        // primary schema attribute will be cleared only for new system (in reopened case is UID attribute not deleted).
        wizardContext.connectorType.metadata.primarySchemaAttributeId = null;
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
    const {showLoading} = this.state;

    const _connectorType = this.state.connectorType ? this.state.connectorType : connectorType;
    const formData = {};
    if (_connectorType && _connectorType.metadata) {
      const metadata = _connectorType.metadata;
      formData.name = metadata.name;
      formData.port = metadata.port;
      formData.host = metadata.host;
      formData.user = metadata.user;
      formData.database = metadata.database;
      formData.table = metadata.table;
      formData.keyColumn = metadata.keyColumn;
      if (_connectorType.reopened) {
        // We expecting the password was already filled for reopened system.
        formData.password = '********';
      }
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
          <Basic.Div style={{display: 'flex'}}>
            <Basic.Div style={{flex: 3, marginRight: 15}}>
              <Basic.TextField
                ref="host"
                label={this.i18n(`${locKey}.host.label`)}
                helpBlock={this.i18n(`${locKey}.host.help`)}
                required
                max={128}/>
            </Basic.Div>
            <Basic.Div style={{flex: 1}}>
              <Basic.TextField
                ref="port"
                validation={Joi.number().integer().min(0).max(65535)}
                label={this.i18n(`${locKey}.port.label`)}
                helpBlock={this.i18n(`${locKey}.port.help`)}
                required/>
            </Basic.Div>
          </Basic.Div>
          <Basic.TextField
            ref="database"
            label={this.i18n(`${locKey}.database.label`)}
            helpBlock={this.i18n(`${locKey}.database.help`)}
            required
            max={128}/>
          <Basic.Div style={{display: 'flex'}}>
            <Basic.Div style={{flex: 1, marginRight: 15}}>
              <Basic.TextField
                ref="table"
                label={this.i18n(`${locKey}.table.label`)}
                helpBlock={this.i18n(`${locKey}.table.help`)}
                required
                max={128}/>
            </Basic.Div>
            <Basic.Div style={{flex: 1}}>
              <Basic.TextField
                ref="keyColumn"
                label={this.i18n(`${locKey}.keyColumn.label`)}
                helpBlock={this.i18n(`${locKey}.keyColumn.help`)}
                required
                max={128}/>
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
