
import { Basic } from 'czechidm-core';
import { SystemManager } from '../../redux';

const systemManager = new SystemManager();

/**
 * Abstraction for wizard step.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
export default class AbstractWizardStep extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    const wizardContext = context.wizardContext;
    this.state = {showLoading: false};
    // If context contains connectorType, then we will used it.
    if (wizardContext && wizardContext.connectorType) {
      this.state.connectorType = wizardContext.connectorType;
    }
  }

  getStepId() {
    return this.props.wizardStepId;
  }

  getLocKey() {
    return `${this.props.baseLocKey}.steps.${this.getStepId()}`;
  }

  getSystemManager() {
    return systemManager;
  }

  wizardNext(event) {
    if (event) {
      event.preventDefault();
    }
    const {connectorType} = this.props;
    const formData = this.refs.form.getData();
    if (!this.refs.form.isFormValid()) {
      return;
    }

    const _connectorType = this.state.connectorType ? this.state.connectorType : connectorType;
    if (_connectorType && _connectorType.metadata) {
      const system = this.context.wizardContext.entity;
      _connectorType.wizardStepName = this.getStepId();
      this.compileMetadata(_connectorType, formData, system);
    }
    this.setState({
      showLoading: true
    }, () => {
      systemManager.getService().executeConnectorType(_connectorType)
        .then((json) => {
          this.setState({
            showLoading: false
          }, () => {
            // Set system to the wizard context.
            const wizardContext = this.context.wizardContext;
            this.afterNextAction(wizardContext, json, _connectorType);
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

  /**
   * Prepare metadata for next action (send to the BE).
   */
  // eslint-disable-next-line no-unused-vars
  compileMetadata(_connectorType, formData, system) {
    //
  }

  /**
   * Is call after execution of the step on backend.
   * Good place for set result to the wizard context.
   */
  // eslint-disable-next-line no-unused-vars
  afterNextAction(wizardContext, connectorType, originalConnectorType) {
    //
  }

}
