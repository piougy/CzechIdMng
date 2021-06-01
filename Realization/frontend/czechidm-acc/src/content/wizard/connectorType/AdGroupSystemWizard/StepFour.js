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
    metadata.domainContainer = formData.domainContainer;
  }

  render() {
    const {connectorType} = this.props;
    const {showLoading} = this.state;

    const _connectorType = this.state.connectorType ? this.state.connectorType : connectorType;
    const formData = {};
    if (_connectorType && _connectorType.metadata) {
      const metadata = _connectorType.metadata;
      formData.groupContainer = metadata.groupContainer ? metadata.groupContainer : null;
      formData.domainContainer = metadata.domainContainer;
    }

    const locKey = this.getLocKey();

    return (
      <Basic.Div showLoading={showLoading}>
        <Basic.AbstractForm
          ref="form"
          data={formData}>
          <Basic.TextField
            ref="groupContainer"
            label={this.i18n(`${locKey}.groupContainer.label`)}
            helpBlock={this.i18n(`${locKey}.groupContainer.help`)}
            required
            max={255}/>
          <Basic.TextField
            ref="domainContainer"
            label={this.i18n(`${locKey}.domainContainer.label`)}
            helpBlock={this.i18n(`${locKey}.domainContainer.help`)}
            required
            max={255}/>
        </Basic.AbstractForm>
      </Basic.Div>
    );
  }
}
