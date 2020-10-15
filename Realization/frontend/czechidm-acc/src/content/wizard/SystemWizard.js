import { Basic } from 'czechidm-core';
import React from 'react';
import IdmContext from 'czechidm-core/src/context/idm-context';

/**
 * Wizard for create a universal system.
 *
 * @author Vít Švanda
 * @since 10.6.0
 */
export default class SystemWizard extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.wizardContext = {};
  }

  /**
   * Returns current wizard steps. Steps are changing dynamically.
   * It means this method is calls in every render of the wizard component.
   */
  getWizardSteps(props, context) {
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
          isDone: true,
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
          label: this.i18n('acc:wizard.create-system.steps.systemDetail.name'),
          help: this.i18n('acc:wizard.create-system.steps.systemDetail.help'),
          getComponent: () => {
            return (route.props.render({match: this.props.match, location: {query: {new: true}}, wizardStepId: stepId}));
          }
        };
      })[0];

    // Connector step
    const stepConnector = routesSystem
      .filter(route => route.props.path === '/system/:entityId/connector').map(route => {
        const stepId = 'connector';
        return {
          id: stepId,
          getComponent: (wizardContext) => (route.props.render(this._buildStepProps(stepId, wizardContext))),
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
    const stepMappingResult = this.getMappingStep(routesSystem, activeStep, context);
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
          isDone: true,
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
      getComponent: (wizardContext) => {
        return (
          <Basic.Div style={{marginTop: 15}}>
            <Basic.Row>
              <Basic.Col lg={2} md={2}/>
              <Basic.Col lg={8} md={8}>
                <Basic.Alert
                  level="success"
                  icon="ok"
                  className="alert-icon-large"
                  text={this.i18n('acc:wizard.create-system.steps.summary.text', { systemName: wizardContext.entity.name, escape: false })}
                  buttons={[
                    <Basic.Button
                      level="success"
                      onClick={this.props.closeWizard.bind(this, true, wizardContext)}
                      title={this.i18n('component.basic.Wizard.button.finish')}>
                      {this.i18n('component.basic.Wizard.button.finish')} »
                    </Basic.Button>
                  ]}
                />
              </Basic.Col>
            </Basic.Row>
          </Basic.Div>
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
   * Get final component for mapping step.
   */
  getMappingStep(routesSystem, activeStep, context) {

    const stepMappingNew = routesSystem
      .filter(route => route.props.path === '/system/:entityId/mappings/:mappingId/new')
      .map(route => {
        const stepId = 'mappingNew';
        return {
          id: stepId,
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
          isDone: true,
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
          getComponent: (wizardContext) => (route.props.render(
            {
              match: this.props.match,
              location: {query: {
                new: true,
                systemId: wizardContext.entity.id,
                objectClassId: activeStep.objectClass.id,
                mappingId: activeStep.mapping.id}
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
    const {show, modal} = this.props;
    const wizardContext = this.wizardContext;

    return (
      <IdmContext.Provider value={{...this.context, wizardContext}}>
        <Basic.Wizard
          getSteps={this.getWizardSteps.bind(this)}
          modal={modal}
          show={show}
          module="acc"
          id="create-system"
          onCloseWizard={this.props.closeWizard}/>
      </IdmContext.Provider>
    );
  }
}
