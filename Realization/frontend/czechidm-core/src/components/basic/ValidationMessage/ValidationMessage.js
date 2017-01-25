import React from 'react';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import Alert from '../Alert/Alert';

/**
 * Parameters in errors. Contain names of not success policies
 * @type {String}
 */
const PASSWORD_POLICIES_NAMES = 'policiesNames';

/**
 * Enchanced control, minimal rules to fulfill.
 * Value of parameter MIN_RULES_TO_FULFILL is map with rules
 * @type {String}
 */
const MIN_RULES_TO_FULFILL = 'minRulesToFulfill';

/**
 * Enchanced control, count with minimal rules to fulfill
 * @type {String}
 */
const MIN_RULES_TO_FULFILL_COUNT = 'minRulesToFulfillCount';

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

    // iterate over all parameters in error
    for (const key in error.parameters) {
      if (error.parameters.hasOwnProperty(key)) {
        // enchanced control special message, minimal rules to fulfill
        if (key === MIN_RULES_TO_FULFILL) {
          // fill rules with not required messages
          let rules = '<ul style="padding-left: 20px">';
          for (const ruleKey in error.parameters[key]) {
            if (error.parameters[key].hasOwnProperty(ruleKey)) {
              rules += '<span><li>' + this.i18n('content.passwordPolicies.validation.' + ruleKey) + error.parameters[key][ruleKey] + '</li>';
            }
          }
          rules += '</ul></span>';
          validationMessage.push(
            <Alert level="warning" >
              <span dangerouslySetInnerHTML={{
                __html: this.i18n('content.passwordPolicies.validation.' + MIN_RULES_TO_FULFILL, {'count': error.parameters[MIN_RULES_TO_FULFILL_COUNT]} ) + ' ' + rules}}
              />
            </Alert>);
        } else if (key !== PASSWORD_POLICIES_NAMES) {
          // other validation messages
          validationMessage.push(<Alert level="warning" >{this.i18n('content.passwordPolicies.validation.' + key) + error.parameters[key]}</Alert>);
        }
      }
    }
    // last message is password policies names
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
