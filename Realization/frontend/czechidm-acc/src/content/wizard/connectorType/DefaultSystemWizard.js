import { Basic, Managers } from 'czechidm-core';
import React from 'react';
import _ from 'lodash';
import { SystemManager } from '../../../redux';
import AbstractWizardStep from '../AbstractWizardStep';
import SystemEntityTypeEnum from '../../../domain/SystemEntityTypeEnum';

const systemManager = new SystemManager();
const roleManager = new Managers.RoleManager();
const treeTypeManager = new Managers.TreeTypeManager();

/**
 * Wizard for create a universal system.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
export default class DefaultSystemWizard extends Basic.AbstractContextComponent {


  constructor(props, context) {
    super(props, context);
    const {connectorType} = props;
    const wizardContext = context.wizardContext;
    if (wizardContext && connectorType) {
      wizardContext.entity = connectorType._embedded.system;
      wizardContext.mapping = connectorType._embedded.mapping;
      wizardContext.syncConfig = connectorType._embedded.sync;
      wizardContext.connectorType = connectorType;
    }
    this.state = {};
  }

  getWizardId() {
    return 'create-system';
  }

  getModule() {
    return 'acc';
  }

  getApiPath() {
    return `/connector-types/${this.getWizardId()}`;
  }

  getBaseLocKey() {
    return `${this.getModule()}:wizard.${this.getWizardId()}`;
  }

  /**
	 * Returns current wizard steps. Steps are changing dynamically.
	 * It means this method is calls in every render of the wizard component.
	 */
  getWizardSteps() {
    const context = this.context;
    let activeStep = null;
    if (context && context.wizardContext) {
      activeStep = context.wizardContext.activeStep;
    }
    const routeFirstLine = this.generateRouteComponents({path: '/'}, {});
    const routesSystem = this.generateRouteComponents({path: '/system/:entityId/'}, {});

    // System detail step
    const stepSystemDetail = routesSystem
      .filter(route => route.props.path === '/system/:entityId/detail')
      .map(route => {
        const stepId = 'systemDetail';
        return {
          id: stepId,
          wizardId: 'create-system',
          isDone: (activeStep && activeStep.id) === 'systemNew',
          getComponent: (wizardContext) => {
            return (route.props.render(this._buildStepProps(stepId, wizardContext)));
          }
        };
      })[0];

    // New system detail step
    const stepNewSystem = routeFirstLine
      .filter(route => route.props.path === '/system/:entityId/new')
      .map(route => {
        const stepId = 'systemNew';
        return {
          id: stepId,
          wizardId: 'create-system',
          label: this.i18n('acc:wizard.create-system.steps.systemDetail.name'),
          help: this.i18n('acc:wizard.create-system.steps.systemDetail.help'),
          getComponent: () => {
            return (route.props.render({match: this.props.match, location: {query: {new: true}}, wizardStepId: stepId}));
          },
          // Good method for execute a step on the backend (if is defined).
          // For example set connector info after new system is created.
          getExecuteConnectorType: (cb) => {
            const wizardContext = this.context.wizardContext;
            const {connectorType} = this.props;
            const _connectorType = _.merge({}, connectorType);
            _connectorType.wizardStepName = 'create-system.systemNew';
            _connectorType.metadata.system = wizardContext.entity ? wizardContext.entity.id : null;

            this.setState({
              showLoading: true
            }, () => {
              systemManager.getService().executeConnectorType(_connectorType)
                .then((json) => {
                  wizardContext.entity = json._embedded && json._embedded.system ? json._embedded.system : wizardContext.entity;
                  this.setState({
                    showLoading: false
                  }, () => {
                    if (cb) {
                      cb();
                    }
                  });
                }).catch(ex => {
                  this.setState({
                    showLoading: false
                  });
                  this.addError(ex);
                });
            });
          }
        };
      })[0];

    // Connector step
    const stepConnector = routesSystem
      .filter(route => route.props.path === '/system/:entityId/connector').map(route => {
        const stepId = 'connector';
        return {
          id: stepId,
          wizardId: 'create-system',
          getComponent: (wizardContext) => {
            return route.props.render(this._buildStepProps(stepId, wizardContext));
          },
          getValidation: (wizardContext) => {
            if (!wizardContext.entity.connectorKey) {
              this.addMessage({
                title: this.i18n('acc:wizard.create-system.steps.connector.validation.missingConnectorKey.title'),
                message: this.i18n('acc:wizard.create-system.steps.connector.validation.missingConnectorKey.text'),
                level: 'warning'
              });
              return false;
            }
            return true;
          }
        };
      })[0];
    // Final component for schema step.
    const stepSchemaResult = this.getSchemaStep(routesSystem, activeStep, context);
    // Final component for mapping step.
    const stepMappingResult = this.getSimpleMappingStep(routesSystem, activeStep, context);
    // Final component for attribute mapping step.
    const stepMappingAttributesResult = this.getMappingAttributeStep(routesSystem, activeStep, context);
    let stepSystemResult = stepNewSystem;

    // If is system already created, then first step will be detail of the system instead the new system.
    if (context && context.wizardContext && context.wizardContext.entity) {
      stepSystemResult = stepSystemDetail;
    }

    // Sync step.
    const stepSyncDetail = routesSystem
      .filter(route => route.props.path === '/system/:entityId/synchronization-configs/:configId/detail')
      .map(route => {
        const stepId = 'sync';
        return {
          id: stepId,
          wizardId: 'create-system',
          isDone: (activeStep && activeStep.id) === 'syncNew',
          getComponent: (wizardContext) => {
            const buildProps = this._buildStepProps(stepId, wizardContext);
            buildProps.match.params.configId = wizardContext.syncConfig.id;

            return (
              <Basic.Div style={{paddingTop: 5}}>
                {route.props.render(buildProps)}
              </Basic.Div>
            );
          }
        };
      })[0];

    // New sync step.
    const stepSyncNew = routesSystem
      .filter(route => route.props.path === '/system/:entityId/synchronization-configs/:configId/new')
      .map(route => {
        const stepId = 'syncNew';
        return {
          id: stepId,
          wizardId: 'create-system',
          isSkipable: true, // Sync step could be skipped.
          label: this.i18n('acc:wizard.create-system.steps.sync.name'),
          help: this.i18n('acc:wizard.create-system.steps.sync.help'),
          getComponent: (wizardContext) => {
            return (
              <Basic.Div style={{paddingTop: 5}}>
                {route.props.render({
                  match: {params: {entityId: wizardContext.entity.id}},
                  location: {query: {new: true, systemId: wizardContext.entity.id}},
                  wizardStepId: stepId
                })}
              </Basic.Div>
            );
          }
        };
      })[0];

    // Final summary step.
    const summary = {
      id: 'summary',
      wizardId: 'create-system',
      getComponent: () => {
        return (
          <SummaryStep
            match={this.props.match}
            reopened={this.props.reopened}
            wizardStepId="summary"
            connectorType={this.props.connectorType}
          />
        );
      }
    };

    // If is sync already created, then last step will be detail of the sync instead the new sync.
    let stepSyncResult = stepSyncNew;
    if (context && context.wizardContext && context.wizardContext.syncConfig) {
      stepSyncResult = stepSyncDetail;
    }
    // Sync step is show only if exists mapping with operation type set to the synchronization.
    if (context.wizardContext.mapping && context.wizardContext.mapping.operationType === 'SYNCHRONIZATION') {
      return [stepSystemResult, stepConnector, stepSchemaResult, stepMappingResult, stepMappingAttributesResult, stepSyncResult, summary];
    }

    return [stepSystemResult, stepConnector, stepSchemaResult, stepMappingResult, stepMappingAttributesResult, summary];
  }

  /**
	 * Get final component for schema step.
	 */
  getSchemaStep(routesSystem, activeStep) {
    const stepSchemas = routesSystem
      .filter(route => route.props.path === '/system/:entityId/object-classes')
      .map(route => {
        const stepId = 'schemas';
        return {
          id: stepId,
          wizardId: 'create-system',
          label: this.i18n('acc:wizard.create-system.steps.schema.name'),
          getComponent: (wizardContext) => (route.props.render(this._buildStepProps(stepId, wizardContext)))
        };
      })[0];
    const stepSchemaNew = routesSystem
      .filter(route => route.props.path === '/system/:entityId/object-classes/:objectClassId/new')
      .map(route => {
        const stepId = 'schemaNew';
        return {
          id: stepId,
          wizardId: 'create-system',
          getComponent: (wizardContext) => (route.props.render(
            {
              match: this.props.match,
              location: {query: {new: true, systemId: wizardContext.entity.id}},
              wizardStepId: stepId
            }
          ))
        };
      })[0];
    const stepSchemaDetail = routesSystem
      .filter(route => route.props.path === '/system/:entityId/object-classes/:objectClassId/detail')
      .map(route => {
        const stepId = 'schema';
        return {
          id: stepId,
          wizardId: 'create-system',
          getComponent: (wizardContext) => {
            const props = this._buildStepProps(stepId, wizardContext);
            props.match.params.objectClassId = wizardContext.activeStep.objectClass.id;
            return route.props.render(props);
          }
        };
      })[0];
    const stepSchemaAttributeNew = routesSystem
      .filter(route => route.props.path === '/system/:entityId/schema-attributes/:attributeId/new')
      .map(route => {
        const stepId = 'schemaAttributeNew';
        return {
          id: stepId,
          wizardId: 'create-system',
          getComponent: (wizardContext) => (route.props.render(
            {
              match: this.props.match,
              location: {
                query: {
                  new: true,
                  systemId: wizardContext.entity.id,
                  objectClassId: activeStep.objectClass.id
                }
              },
              wizardStepId: stepId
            }
          ))
        };
      })[0];
    const stepSchemaAttributeDetail = routesSystem
      .filter(route => route.props.path === '/system/:entityId/schema-attributes/:attributeId/detail')
      .map(route => {
        const stepId = 'schemaAttribute';
        return {
          id: stepId,
          wizardId: 'create-system',
          getComponent: (wizardContext) => {
            const props = this._buildStepProps(stepId, wizardContext);
            props.match.params.attributeId = wizardContext.activeStep.attributeId.id;
            return route.props.render(props);
          }
        };
      })[0];

    let stepSchemaResult = stepSchemas;
    if (activeStep && activeStep.id === stepSchemaNew.id) {
      stepSchemaResult = stepSchemaNew;
    }
    if (activeStep && activeStep.id === stepSchemaDetail.id) {
      stepSchemaResult = stepSchemaDetail;
    }
    if (activeStep && activeStep.id === stepSchemaAttributeDetail.id) {
      stepSchemaResult = stepSchemaAttributeDetail;
    }
    if (activeStep && activeStep.id === stepSchemaAttributeNew.id) {
      stepSchemaResult = stepSchemaAttributeNew;
    }
    return stepSchemaResult;
  }

  /**
	 * Get simple mapping step.
	 */
  getSimpleMappingStep() {
    const stepId = 'mapping';
    return (
      {
        id: stepId,
        label: this.i18n('acc:wizard.create-system.steps.mapping.name'),
        help: this.i18n('acc:wizard.create-system.steps.mapping.help'),
        getComponent: () => {
          return (
            <SimpleMapping
              match={this.props.match}
              wizardStepId={stepId}
              connectorType={this.props.connectorType}
              baseLocKey={this.getBaseLocKey()}
            />
          );
        }
      }
    );
  }

  /**
	 * Get final component for mapping step.
   * Full mapping - @Deprecated now
	 */
  getMappingStep(routesSystem, activeStep, context) {

    const stepMappingNew = routesSystem
      .filter(route => route.props.path === '/system/:entityId/mappings/:mappingId/new')
      .map(route => {
        const stepId = 'mappingNew';
        return {
          id: stepId,
          wizardId: 'create-system',
          label: this.i18n('acc:wizard.create-system.steps.mapping.name'),
          help: this.i18n('acc:wizard.create-system.steps.mapping.help'),
          getComponent: (wizardContext) => (route.props.render(
            {
              match: {params: {entityId: wizardContext.entity.id}},
              location: {query: {new: true, systemId: wizardContext.entity.id}},
              wizardStepId: stepId
            }
          )
          )
        };
      })[0];
    const stepMappingDetail = routesSystem
      .filter(route => route.props.path === '/system/:entityId/mappings/:mappingId/detail')
      .map(route => {
        const stepId = 'mapping';
        return {
          id: stepId,
          wizardId: 'create-system',
          isDone: (activeStep && activeStep.id) === 'mappingNew',
          getComponent: (wizardContext) => {
            const props = this._buildStepProps(stepId, wizardContext);
            props.showOnlyMapping = true;
            props.match.params.mappingId = wizardContext.mapping.id;
            return route.props.render(props);
          }
        };
      })[0];

    let stepMappingResult = stepMappingNew;
    // If is mapping already created, then detail of the mapping instead the new mapping will be used.
    if (context && context.wizardContext && context.wizardContext.mapping) {
      stepMappingResult = stepMappingDetail;
    }
    return stepMappingResult;
  }

  /**
	 * Get final component for attribute mapping step.
	 */
  getMappingAttributeStep(routesSystem, activeStep) {

    const stepMappingDetail = routesSystem
      .filter(route => route.props.path === '/system/:entityId/mappings/:mappingId/detail')
      .map(route => {
        // Step ID is not only route path, but has postfix /attributes, because same component was already used.
        const stepId = 'mappingAttributes';
        return {
          id: stepId,
          wizardId: 'create-system',
          getComponent: (wizardContext) => {
            const props = this._buildStepProps(stepId, wizardContext);
            props.showOnlyAttributes = true;
            props.match.params.mappingId = wizardContext.mapping.id;
            return route.props.render(props);
          }
        };
      })[0];
    const stepMappingAttributeNew = routesSystem
      .filter(route => route.props.path === '/system/:entityId/attribute-mappings/:attributeId/new')
      .map(route => {
        const stepId = 'mappingAttributeNew';
        return {
          id: stepId,
          wizardId: 'create-system',
          getComponent: (wizardContext) => (route.props.render(
            {
              match: this.props.match,
              location: {
                query: {
                  new: true,
                  systemId: wizardContext.entity.id,
                  objectClassId: activeStep.objectClass.id,
                  mappingId: activeStep.mapping.id
                }
              },
              wizardStepId: stepId
            }
          ))
        };
      })[0];
    const stepMappingAttributeDetail = routesSystem
      .filter(route => route.props.path === '/system/:entityId/attribute-mappings/:attributeId/detail')
      .map(route => {
        const stepId = 'mappingAttribute';
        return {
          id: stepId,
          wizardId: 'create-system',
          getComponent: (wizardContext) => {
            const props = this._buildStepProps(stepId, wizardContext);
            props.match.params.attributeId = wizardContext.activeStep.attribute.id;
            return route.props.render(props);
          }
        };
      })[0];

    let stepMappingResult = stepMappingDetail;
    if (activeStep && activeStep.id === stepMappingAttributeDetail.id) {
      stepMappingResult = stepMappingAttributeDetail;
    }
    if (activeStep && activeStep.id === stepMappingAttributeNew.id) {
      stepMappingResult = stepMappingAttributeNew;
    }
    return stepMappingResult;
  }

  _buildStepProps(stepId, wizardContext) {
    return {
      match: {...this.props.match, params: {entityId: wizardContext.entity ? wizardContext.entity.id : null}},
      location: this.props.location,
      entity: wizardContext.entity,
      wizardStepId: stepId
    };
  }

  render() {
    const {show, modal, connectorType} = this.props;
    const {showLoading} = this.state;
    return (
      <Basic.Wizard
        getSteps={this.getWizardSteps.bind(this)}
        modal={modal}
        name={connectorType
          && connectorType._embedded
          && connectorType._embedded.system
           ? connectorType._embedded.system.name
           : null}
        showLoading={showLoading}
        show={show}
        icon={connectorType ? connectorType.iconKey : null}
        module={this.getModule()}
        id={this.getWizardId()}
        onCloseWizard={this.props.closeWizard}/>
    );
  }
}

/**
 * The last step in a system wizard.
 */
class SummaryStep extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    const wizardContext = context.wizardContext;
    const system = wizardContext.entity;
    const operationType = wizardContext.mapping ? wizardContext.mapping.operationType : null;
    this.state = {
      showLoading: false,
      mappingSystemOnRole: operationType === 'PROVISIONING' && !props.reopened,
      newRoleWithSystem: system ? `${system.name}-users` : null
    };
    // If context contains connectorType, then we will used it.
    if (wizardContext && wizardContext.connectorType) {
      this.state.connectorType = wizardContext.connectorType;
    }
  }

  wizardNext() {
    const {connectorType} = this.props;
    const form = this.refs.form;
    const formData = form ? form.getData() : null;
    if (form && !form.isFormValid()) {
      return;
    }

    const _connectorType = this.state.connectorType ? this.state.connectorType : connectorType;
    if (_connectorType && _connectorType.metadata) {
      _connectorType.wizardStepName = 'finish';
      const system = this.context.wizardContext.entity;
      const mapping = this.context.wizardContext.mapping;
      const metadata = _connectorType.metadata;
      metadata.newRoleWithSystemCode = formData ? formData.newRoleWithSystem : null;
      metadata.createRoleWithSystem = formData ? formData.mappingSystemOnRole : false;
      metadata.system = system ? system.id : null;
      metadata.mappingId = mapping ? mapping.id : null;
    }
    this.setState({
      showLoading: true
    }, () => {
      systemManager.getService().executeConnectorType(_connectorType)
        .then(() => {
          this.setState({
            showLoading: false
          }, () => {
            const wizardContext = this.context.wizardContext;
            wizardContext.connectorType = _connectorType;
            if (wizardContext.callBackNext) {
              wizardContext.callBackNext();
            } else if (wizardContext.onClickNext) {
              wizardContext.onClickNext(false, true);
            }
          });
        }).catch(ex => {
          this.setState({
            showLoading: false
          });
          this.addError(ex);
        });
    });
  }

  _toggleMappingSystemOnRole() {
    this.setState({
      mappingSystemOnRole: !this.refs.mappingSystemOnRole.getValue()
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
    const {showLoading, mappingSystemOnRole, newRoleWithSystem, roleWithSystem} = this.state;
    const {wizardContext} = this.context;

    const formData = {mappingSystemOnRole, newRoleWithSystem, roleWithSystem};
    const systemName = wizardContext.entity ? wizardContext.entity.name : null;
    const entityType = wizardContext.mapping ? wizardContext.mapping.entityType : null;

    const locKey = `acc:wizard.create-system.steps.summary`;

    return (
      <Basic.Div style={{marginTop: 15}} showLoading={showLoading}>
        <Basic.Row>
          <Basic.Col lg={1} md={1}/>
          <Basic.Col lg={10} md={10}>
            <Basic.Alert
              level="success"
              icon="ok"
              className="alert-icon-large"
              text={this.i18n(`${locKey}.text`, {systemName, escape: false})}
            />
          </Basic.Col>
        </Basic.Row>
        <Basic.Row rendered={entityType === 'IDENTITY'}>
          <Basic.Col lg={1} md={1}/>
          <Basic.Col lg={10} md={10}>
            <Basic.Panel style={{backgroundColor: '#d9edf7', borderColor: '#bce8f1', color: '#31708f'}}>
              <Basic.PanelBody>
                <Basic.AbstractForm
                  data={formData}
                  ref="form">
                  <Basic.ToggleSwitch
                    ref="mappingSystemOnRole"
                    label={this.i18n(`${locKey}.mappingSystemOnRole.label`)}
                    helpBlock={this.i18n(`${locKey}.mappingSystemOnRole.help`)}
                    onChange={this._toggleMappingSystemOnRole.bind(this)}
                  />
                  <Basic.Row>
                    <Basic.Col lg={ 5 } md={ 5 }>
                      <Basic.TextField
                        ref="newRoleWithSystem"
                        required={!!mappingSystemOnRole}
                        readOnly={!!roleWithSystem}
                        hidden={!mappingSystemOnRole}
                        label={this.i18n(`${locKey}.newRoleWithSystem.label`)}
                        onChange={this._onChangeNewRoleWithSystem.bind(this)}
                        helpBlock={this.i18n(`${locKey}.newRoleWithSystem.help`)}/>
                    </Basic.Col>
                    <Basic.Col lg={ 7 } md={ 7 }>
                      <Basic.SelectBox
                        ref="roleWithSystem"
                        hidden={!mappingSystemOnRole}
                        manager={roleManager}
                        onChange={this._onChangeRoleWithSystem.bind(this)}
                        label={this.i18n(`${locKey}.roleWithSystem.label`)}/>
                    </Basic.Col>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Basic.PanelBody>
            </Basic.Panel>
          </Basic.Col>
        </Basic.Row>
      </Basic.Div>
    );
  }
}

class SimpleMapping extends AbstractWizardStep {

  constructor(props, context) {
    super(props, context);
    this.state.provisioningSwitch = true;
    const mapping = this.context.wizardContext.mapping;
    if (mapping) {
      const metadata = this.state.connectorType.metadata;
      metadata.mappingId = mapping ? mapping.id : null;
      metadata.entityType = mapping.entityType;
      metadata.treeTypeId = mapping.treeType;
      this.state.provisioningSwitch = mapping.operationType === 'PROVISIONING';
      this.state.syncSwitch = mapping.operationType !== 'PROVISIONING';
    }
  }

  /**
   * Prepare metadata for next action (send to the BE).
   */
  compileMetadata(_connectorType, formData, system) {
    const metadata = _connectorType.metadata;
    const mapping = this.context.wizardContext.mapping;
    metadata.system = system ? system.id : null;
    metadata.mappingId = mapping ? mapping.id : null;
    metadata.entityType = formData.entityType;
    metadata.treeTypeId = formData.treeType;
    metadata.operationType = formData.provisioningSwitch ?
      'PROVISIONING' : 'SYNCHRONIZATION';
  }

  /**
   * Is call after execution of the step on backend.
   * Good place for set result to the wizard context.
   */
  afterNextAction(wizardContext, json) {
    wizardContext.mapping = json._embedded.mapping;
    wizardContext.connectorType = json;
  }

  _toggleProvisioningSwitch() {
    this.setState({
      provisioningSwitch: !this.refs.provisioningSwitch.getValue(),
      syncSwitch: this.refs.provisioningSwitch.getValue()
    });
  }

  _toggleSyncSwitch() {
    this.setState({
      syncSwitch: !this.refs.syncSwitch.getValue(),
      provisioningSwitch: this.refs.syncSwitch.getValue()
    });
  }

  _onChangeEntityType(entity) {
    this.setState({entityType: entity.value});
  }

  render() {
    const {connectorType} = this.props;
    const {showLoading, provisioningSwitch, syncSwitch, entityType} = this.state;

    const _connectorType = this.state.connectorType ? this.state.connectorType : connectorType;
    const formData = {};
    let showAlertMoreMappings = false;
    if (_connectorType && _connectorType.metadata) {
      const metadata = _connectorType.metadata;
      formData.provisioningSwitch = provisioningSwitch;
      formData.syncSwitch = syncSwitch;
      formData.treeType = metadata.treeTypeId;
      formData.entityType = metadata.entityType ? metadata.entityType : 'IDENTITY';
      showAlertMoreMappings = metadata.alertMoreMappings;
    }

    const _entityType = entityType || formData.entityType;
    const isSelectedTree = _entityType === 'TREE';
    const locKey = 'acc:wizard.create-system.steps.mapping';

    return (
      <Basic.Div showLoading={showLoading}>
        <Basic.Alert
          rendered={!!showAlertMoreMappings}
          text={this.i18n(`${locKey}.alertMoreMappings`, {
            systemId: this.context.wizardContext.entity ? this.context.wizardContext.entity.id : null})}
          icon="exclamation-sign"
          showHtmlText
          level="warning"
        />
        <Basic.AbstractForm
          ref="form"
          onSubmit={(event) => {
            this.wizardNext(event);
          }}
          data={formData}>
          <Basic.ContentHeader style={{marginTop: 15}}>
            {this.i18n(`${locKey}.operationTypeHeader`)}
          </Basic.ContentHeader>
          <Basic.Div
            style={{padding: 15}}>
            <Basic.Div style={{display: 'flex'}}>
              <Basic.Div style={{flex: 5}}>
                <Basic.ToggleSwitch
                  ref="provisioningSwitch"
                  label={this.i18n(`${locKey}.provisioningSwitch.label`)}
                  helpBlock={this.i18n(`${locKey}.provisioningSwitch.help`)}
                  onChange={this._toggleProvisioningSwitch.bind(this)}
                />
                <Basic.ToggleSwitch
                  ref="syncSwitch"
                  label={this.i18n(`${locKey}.syncSwitch.label`)}
                  helpBlock={this.i18n(`${locKey}.syncSwitch.help`)}
                  onChange={this._toggleSyncSwitch.bind(this)}
                />
              </Basic.Div>
              <Basic.Div style={{flex: 3}}>
                <Basic.Div style={{display: 'flex', marginTop: 50}}>
                  <Basic.Div style={{flex: 1}}>
                    <img style={{maxWidth: 120}} src="dist/images/czechidm-logo-big.png" alt="IdM logo"/>
                  </Basic.Div>
                  <Basic.Div style={{flex: 1, textAlign: 'center', fontSize: 'x-large'}}>
                    <Basic.Icon
                      icon={provisioningSwitch ? 'fa:angle-double-right' : 'fa:angle-double-left'}/>
                  </Basic.Div>
                  <Basic.Div style={{flex: 1}}>
                    <Basic.Icon
                      type="component"
                      iconStyle="sm"
                      icon={_connectorType.iconKey}/>
                  </Basic.Div>
                </Basic.Div>
              </Basic.Div>
            </Basic.Div>
          </Basic.Div>
          <Basic.ContentHeader>
            {this.i18n(`${locKey}.entityTypeHeader`)}
          </Basic.ContentHeader>
          <Basic.Div
            style={{marginLeft: 15, maxWidth: 340}}>
            <Basic.EnumSelectBox
              ref="entityType"
              enum={SystemEntityTypeEnum}
              onChange={this._onChangeEntityType.bind(this)}
              required
              clearable={false}/>
            <Basic.SelectBox
              ref="treeType"
              label={this.i18n('acc:entity.SystemMapping.treeType')}
              hidden={!isSelectedTree}
              required={isSelectedTree}
              manager={treeTypeManager}
            />
          </Basic.Div>
        </Basic.AbstractForm>
      </Basic.Div>
    );
  }
}
