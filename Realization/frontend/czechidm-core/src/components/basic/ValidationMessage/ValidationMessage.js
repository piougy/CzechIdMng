import React from 'react';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import Alert from '../Alert/Alert';

/**
 * Parameters in errors. Contain names of not success policies
 * @type {String}
 */
const PASSWORD_POLICIES_NAMES = 'policiesNames';

export default class ValidationMessage extends AbstractFormComponent {

  constructor(props) {
    super(props);
  }

  componentDidMount() {
    const { error } = this.props;
    this._prepareValidationMessage(error);
  }

  componentWillReciveNewProps(nextProps) {
    const { error } = this.props;

    if (error !== nextProps.error) {
      this._prepareValidationMessage(nextProps.error);
    }
  }

  _prepareValidationMessage(error) {
    if (!error || !error.parameters) {
      return null;
    }

    const validationMessage = [];
    let policies = '';

    for (const key in error.parameters) {
      if (error.parameters.hasOwnProperty(key)) {
        if (key !== PASSWORD_POLICIES_NAMES) {
          validationMessage.push(<Alert level="warning" >{this.i18n('content.passwordPolicies.validation.' + key) + error.parameters[key]}</Alert>);
        }
      }
    }
    if (error.parameters.hasOwnProperty(PASSWORD_POLICIES_NAMES)) {
      policies = this.i18n('content.passwordPolicies.validation.' + PASSWORD_POLICIES_NAMES) + error.parameters[PASSWORD_POLICIES_NAMES];
      validationMessage.push(<Alert level="warning" >{policies}</Alert>);
    }

    return validationMessage;
  }

  render() {
    const { rendered, error } = this.props;

    if (!rendered || !error) {
      return null;
    }

    const validation = this._prepareValidationMessage(error);

    return (
      <div>
        {validation}
      </div>
    );
  }
}

ValidationMessage.propTypes = {
  ...AbstractFormComponent.propTypes,
  error: React.PropTypes.object
};

ValidationMessage.defaultProps = {
  ...AbstractFormComponent.defaultProps
};
