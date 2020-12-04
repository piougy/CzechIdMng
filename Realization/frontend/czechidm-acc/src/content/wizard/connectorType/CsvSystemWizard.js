import { Basic, Services, Utils, Domain } from 'czechidm-core';
import React from 'react';
import _ from 'lodash';
import { SchemaAttributeManager } from '../../../redux';
import DefaultSystemWizard from './DefaultSystemWizard';
import AbstractWizardStep from '../AbstractWizardStep';

const schemaAttributeManager = new SchemaAttributeManager();

/**
 * Wizard for create a CSV system.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
export default class CsvSystemWizard extends DefaultSystemWizard {

  constructor(props, context) {
    super(props, context);
    this.state = {showWizard: false};
    this.wizardContext = {};
  }

  getWizardId() {
    return 'csv-connector-type';
  }

  getWizardSteps(props, context) {
    const steps = super.getWizardSteps(props, context);
    const stepOneId = 'csvStepOne';
    const csvStepOne = {
      id: stepOneId,
      getComponent: () => {
        return (
          <CsvStepOne
            match={this.props.match}
            wizardStepId={stepOneId}
            connectorType={this.props.connectorType}
            apiPath={this.getApiPath()}
            baseLocKey={this.getBaseLocKey()}
          />
        );
      }
    };
    const stepTwoId = 'csvStepTwo';
    const csvStepTwo = {
      id: stepTwoId,
      getComponent: () => {
        return (
          <CsvStepTwo
            match={this.props.match}
            wizardStepId={stepTwoId}
            connectorType={this.props.connectorType}
            baseLocKey={this.getBaseLocKey()}
          />
        );
      }
    };
    // Replace first step.
    steps[0] = csvStepOne;
    // Replace second step.
    steps[1] = csvStepTwo;
    // Remove schema step.
    steps.splice(2, 1);
    return [...steps];
  }
}

/**
 * First step of CSV connector - Creates a system, uploads a CSV file, generate schema.
 */
class CsvStepOne extends AbstractWizardStep {

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
    metadata.filePath = formData.filePath;
    metadata.system = system ? system.id : null;
    metadata.name = formData.name;
    metadata.separator = formData.separator;
  }

  /**
   * Is call after execution of the step on backend.
   * Good place for set result to the wizard context.
   */
  afterNextAction(wizardContext, json) {
    wizardContext.entity = json._embedded.system;
    wizardContext.connectorType = json;
  }

  /**
	 * Dropzone component function called after select file
	 * @param  {array} files Array of selected files
	 */
  _onDrop(files) {
    const locKey = this.getLocKey();
    if (this.refs.dropzone.state.isDragReject) {
      this.addMessage({
        message: this.i18n('filesRejected'),
        level: 'warning'
      });
      return;
    }
    files.forEach((file) => {
      const fileName = file.name.toLowerCase();
      if (!fileName.endsWith('.csv')) {
        this.addMessage({
          message: this.i18n(`${locKey}.fileRejected`, {name: file.name}),
          level: 'warning'
        });
        return;
      }
      let _connectorType = this.state.connectorType;
      this.setState({
        showLoading: true
      }, () => {
        const formData = new FormData();
        formData.append('name', file.name);
        formData.append('goalPath', this.refs.filePath ? this.refs.filePath.getValue() : null);
        formData.append('fileName', file.name);
        formData.append('data', file);
        //
        this.deploy(formData)
          .then((connectorType) => {
            if (!_connectorType) {
              _connectorType = connectorType;
            }
            const formConnectorData = this.refs.form.getData();
            const metadata = _connectorType.metadata;
            metadata.name = formConnectorData.name;
            metadata.separator = formConnectorData.separator;
            // We need to change/fill only filePath from the response.
            metadata.filePath = connectorType.metadata.filePath;

            this.setState({
              connectorType: _connectorType,
              showLoading: false
            }, () => {
              this.addMessage({
                message: this.i18n(`${locKey}.fileUploaded`, {name: file.name})
              });
            });
          })
          .catch(error => {
            this.setState({
              showLoading: false
            });
            this.addError(error);
          });
      });
    });
  }

  deploy(formData) {
    return Services.RestApiService
      .upload(`${this.props.apiPath}/deploy`, formData)
      .then(response => {
        if (response.status === 204) {
          return {};
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        if (Utils.Response.hasInfo(json)) {
          throw Utils.Response.getFirstInfo(json);
        }
        return json;
      });
  }

  render() {
    const {connectorType} = this.props;
    const {showLoading} = this.state;

    const _connectorType = this.state.connectorType ? this.state.connectorType : connectorType;
    let formData = {};
    if (_connectorType && _connectorType.metadata) {
      formData = _.merge({}, _connectorType.metadata);
    }
    const locKey = this.getLocKey();
    return (
      <Basic.Div showLoading={showLoading}>
        <Basic.AbstractForm
          ref="form"
          data={formData}>
          <Basic.TextField
            ref="name"
            label={this.i18n(`${locKey}.systemName`)}
            required
            max={255}/>
          <Basic.TextField
            ref="filePath"
            label={this.i18n(`${locKey}.filePath`)}
            required
            max={255}/>
          <Basic.TextField
            ref="separator"
            label={this.i18n(`${locKey}.separator`)}
            required
            max={1}/>
          <Basic.Dropzone
            ref="dropzone"
            multiple={false}
            accept=".csv"
            onDrop={this._onDrop.bind(this)}>
            {this.i18n(`${locKey}.dropzone.infoText`)}
          </Basic.Dropzone>
        </Basic.AbstractForm>
      </Basic.Div>
    );
  }
}

/**
 * Second step of CSV connector - Selects of primary attribute.
 */
class CsvStepTwo extends AbstractWizardStep {

  constructor(props, context) {
    super(props, context);
    const wizardContext = context.wizardContext;
    this.state = {showLoading: false};
    // If context contains connectorType, then we will used it.
    if (wizardContext && wizardContext.connectorType) {
      this.state.connectorType = wizardContext.connectorType;
    }
  }

  /**
   * Prepare metadata for next action (send to the BE).
   */
  compileMetadata(_connectorType, formData, system) {
    const metadata = _connectorType.metadata;
    metadata.primarySchemaAttributeId = formData.primarySchemaAttributeId;
    metadata.system = system ? system.id : null;
  }

  /**
   * Is call after execution of the step on backend.
   * Good place for set result to the wizard context.
   */
  afterNextAction(wizardContext, connectorType, originalCnnectorType) {
    wizardContext.connectorType = originalCnnectorType;
  }

  render() {
    const {connectorType, baseLocKey} = this.props;
    const {showLoading} = this.state;

    const _connectorType = this.state.connectorType ? this.state.connectorType : connectorType;
    let formData = {};
    if (_connectorType && _connectorType.metadata) {
      formData = _.merge({}, _connectorType.metadata);
    }
    const locKey = `${baseLocKey}.steps.csvStepTwo`;

    const forceSearchParameters = new Domain.SearchParameters()
      .setFilter('objectClassId', _connectorType && _connectorType.metadata.schemaId
				? _connectorType.metadata.schemaId : Domain.SearchParameters.BLANK_UUID);

    return (
      <Basic.Div showLoading={showLoading}>
        <Basic.AbstractForm
          data={formData}
          ref="form">
          <Basic.SelectBox
            ref="primarySchemaAttributeId"
            style={{marginTop: 15, marginLeft: 0, marginRight: 0}}
            manager={schemaAttributeManager}
            forceSearchParameters={forceSearchParameters}
            label={this.i18n(`${locKey}.primarySchemaAttributeId`)}
            required
            pageSize={Domain.SearchParameters.MAX_SIZE}/>
        </Basic.AbstractForm>
      </Basic.Div>
    );
  }
}
