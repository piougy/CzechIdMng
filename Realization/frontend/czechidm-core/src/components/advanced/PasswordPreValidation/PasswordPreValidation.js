import React from 'react';
//
import * as Basic from '../../basic';
import ValidationMessage from '../ValidationMessage/ValidationMessage';

/**
 * @author Patrik Stloukal
 */
export default class PasswordPreValidation extends Basic.AbstractFormComponent {

  constructor(props) {
    super(props);
  }

  render() {
    const { rendered, error, validationDefinition } = this.props;
    if (!rendered || !error) {
      return null;
    }

    return (
      <Basic.Alert
        icon="info-sign"
        text={ this.i18n('content.passwordPolicies.validation.passwordHintPreValidate') }
        style={{ marginBottom: 0 }}>
        <Basic.Popover
          ref="popover"
          trigger={['click']}
          value={
            <Basic.Panel level="info">
              <Basic.PanelHeader>
                {this.i18n('content.passwordPolicies.validation.passwordHintPreValidateHeader')}
              </Basic.PanelHeader>
              <Basic.PanelBody>
                <ValidationMessage error={error} validationDefinition={validationDefinition} />
              </Basic.PanelBody>
            </Basic.Panel>
          }
          className="abstract-entity-info-popover"
          placement="right">
          {
            <Basic.Button
              level="link"
              style={{ padding: 0, whiteSpace: 'normal', verticalAlign: 'baseline' }}
              title={ this.i18n('content.passwordPolicies.validation.prevalidationLink.title') }>
              { this.i18n('content.passwordPolicies.validation.passwordHintPreValidatePwd') }
            </Basic.Button>
          }
        </Basic.Popover>
      </Basic.Alert>
    );
  }
}

PasswordPreValidation.propTypes = {
  ...Basic.AbstractFormComponent.propTypes,
  error: React.PropTypes.object,
  validationDefinition: React.PropTypes.object
};

PasswordPreValidation.defaultProps = {
  ...Basic.AbstractFormComponent.defaultProps,
  validationDefinition: true
};
