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

  componentDidMount() {
    const { error, validationDefinition } = this.props;
    this._getPopoverContent(error, validationDefinition);
  }

  componentWillReciveNewProps(nextProps) {
    const { error, validationDefinition } = this.props;

    if (error !== nextProps.error || validationDefinition !== nextProps.validationDefinition) {
      this._getPopoverContent(nextProps.error, nextProps.validationDefinition);
    }
  }

  _getPopoverContent(validationError, validationDefinition) {
    return (
      <Basic.Panel>
          <Basic.PanelHeader>
            {this.i18n('content.passwordPolicies.validation.passwordHintPreValidateHeader')}
          </Basic.PanelHeader>
        <ValidationMessage error={validationError} validationDefinition={validationDefinition} />
      </Basic.Panel>
  );
  }

  render() {
    const { rendered, error, validationDefinition } = this.props;
    if (!rendered || !error) {
      return null;
    }

    return (
      <Basic.Alert
        icon="info-sign"
        text={this.i18n('content.passwordPolicies.validation.passwordHintPreValidate')}
        style={{ margin: '15px 0'}}>
        <Basic.Popover
          ref="popover"
          trigger={['click']}
          value={ this._getPopoverContent(error, validationDefinition) }
          className="abstract-entity-info-popover"
          placement="right"
          >
          {
              <Basic.Button
                level="link"
                style={{ padding: 0, marginLeft: 3, marginBottom: 5 }}
                title={ this.i18n('content.passwordPolicies.validation.prevalidationLink.title') }>
                {this.i18n('content.passwordPolicies.validation.passwordHintPreValidatePwd')}
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
