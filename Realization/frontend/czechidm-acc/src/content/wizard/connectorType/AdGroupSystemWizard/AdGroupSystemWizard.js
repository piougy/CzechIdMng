import React from 'react';
import DefaultSystemWizard from '../DefaultSystemWizard';
import StepOne from './StepOne';
import StepTwo from './StepTwo';
import StepThree from './StepThree';
import StepFour from './StepFour';

/**
 * Wizard for create a system for administration groups from a MS AD.
 *
 * @author Vít Švanda
 * @since 11.1.0
 */
export default class AdGroupSystemWizard extends DefaultSystemWizard {

  constructor(props, context) {
    super(props, context);
    this.state = {showWizard: false};
    this.wizardContext = {};
  }

  getWizardId() {
    return 'ad-group-connector-type';
  }

  getModule() {
    return 'acc';
  }

  getWizardSteps(props, context) {
    const stepsDefault = super.getWizardSteps(props, context);
    // Remove name, connector, schema, mapping steps.
    stepsDefault.splice(0, 4);

    const steps = [];
    const stepOneId = 'stepOne';
    // Replace first step.
    const stepOne = {
      id: stepOneId,
      getComponent: () => {
        return (
          <StepOne
            match={this.props.match}
            wizardStepId={stepOneId}
            connectorType={this.props.connectorType}
            baseLocKey={this.getBaseLocKey()}
          />
        );
      }
    };
    steps.splice(0, 0, stepOne);

    // Certificate step
    const stepTwoId = 'stepTwo';
    const stepsTwo = {
      id: stepTwoId,
      getComponent: () => {
        return (
          <StepTwo
            match={this.props.match}
            wizardStepId={stepTwoId}
            connectorType={this.props.connectorType}
            apiPath={this.getApiPath()}
            baseLocKey={this.getBaseLocKey()}
          />
        );
      }
    };
    steps.splice(1, 0, stepsTwo);

    // If no trusted CA was found, then wizard has only two steps.
    if (this.context.wizardContext.activeStep
      && this.context.wizardContext.activeStep.id === 'stepTwo'
      && this.context.wizardContext.connectorType.metadata.hasTrustedCa === 'false'
      && this.context.wizardContext.connectorType.metadata.sslSwitch === 'true') {
      // If certificate is missing, then next button have to be hidden.
      steps[1].isLast = true;
      steps[1].hideFinishBtn = true;
    }

    // Permissions step
    const stepThreeId = 'stepThree';
    const stepsThree = {
      id: stepThreeId,
      getComponent: () => {
        return (
          <StepThree
            match={this.props.match}
            wizardStepId={stepThreeId}
            connectorType={this.props.connectorType}
            apiPath={this.getApiPath()}
            baseLocKey={this.getBaseLocKey()}
          />
        );
      }
    };
    steps.splice(2, 0, stepsThree);

    const stepFourId = 'stepFour';
    const stepFour = {
      id: stepFourId,
      getComponent: () => {
        return (
          <StepFour
            match={this.props.match}
            wizardStepId={stepFourId}
            reopened={this.props.reopened}
            connectorType={this.props.connectorType}
            apiPath={this.getApiPath()}
            baseLocKey={this.getBaseLocKey()}
          />
        );
      }
    };
    steps.splice(3, 0, stepFour);

    steps.splice(steps.length, 0, ...stepsDefault);
    return [...steps];
  }
}
