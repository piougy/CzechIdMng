import { Basic, ComponentService } from 'czechidm-core';
import React from 'react';
import IdmContext from 'czechidm-core/src/context/idm-context';
import DefaultSystemWizard from './connectorType/DefaultSystemWizard';

const componentService = new ComponentService();

/**
 * Detail of a system wizard.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
export default class SystemWizardDetail extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.wizardContext = {};
  }

  render() {
    const {show, connectorType, closeWizard, reopened} = this.props;
    const wizardContext = this.wizardContext;
    let ConnectorTypeComponent = DefaultSystemWizard;
    if (connectorType) {
      const component = componentService.getConnectorTypeComponent(connectorType.id);
      if (component) {
        ConnectorTypeComponent = component.component;
      }
    }

    return (
      <Basic.Div rendered={!!show}>
        <IdmContext.Provider value={{...this.context, wizardContext}}>
          <ConnectorTypeComponent
            match={this.props.match}
            modal
            reopened={reopened}
            closeWizard={closeWizard}
            connectorType={connectorType}
            show={!!show}/>
        </IdmContext.Provider>
      </Basic.Div>
    );
  }

}

SystemWizardDetail.defaultProps = {
  reopened: false // Defines if the wizard use for create new system or for reopen existed.
};
