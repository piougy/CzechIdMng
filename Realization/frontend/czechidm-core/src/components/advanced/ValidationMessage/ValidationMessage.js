import React from 'react';
import _ from 'lodash';
//
import * as Basic from '../../basic';
import DateValue from '../DateValue/DateValue';

/**
 * Parameters in errors. Contain names of not success policies
 * @type {String}
 */
const PASSWORD_POLICIES_NAMES = 'policiesNames';

const POLICY_NAME_PREVALIDATION = 'policiesNamesPreValidation';

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

/**
 * Special character base for each required policies
 * @type {String}
 */
const SPECIAL_CHARACTER_BASE = 'specialCharacterBase';

/**
 * Error message with date, date is format by local
 * @type {String}
 */
const DATE = 'date';

/**
 * Array with all validation message parameters, when you add some validation warrning into BE,
 * you also must add this parameters into this arrray.
 * @type {Array}
 */
const VALIDATION_WARNINGS = ['minLength', 'maxLength', 'minUpperChar',
'minLowerChar', 'minNumber', 'minSpecialChar', 'prohibited', 'weakPass',
'minRulesToFulfill', 'minRulesToFulfillCount', 'policiesNames',
'passwordSimilarUsername', 'passwordSimilarEmail', 'passwordSimilarFirstName',
'passwordSimilarLastName', 'maxHistorySimilar',
'passwordSimilarUsernamePreValidate', 'passwordSimilarEmailPreValidate',
'passwordSimilarFirstNamePreValidate', 'passwordSimilarLastNamePreValidate'];

/**
 * @author Ondřej Kopr
 */
export default class ValidationMessage extends Basic.AbstractFormComponent {

  constructor(props) {
    super(props);
  }

  componentDidMount() {
    const { error, validationDefinition } = this.props;
    this._prepareValidationMessage(error, validationDefinition);
  }

  componentWillReciveNewProps(nextProps) {
    const { error, validationDefinition } = this.props;

    if (error !== nextProps.error || validationDefinition !== nextProps.validationDefinition) {
      this._prepareValidationMessage(nextProps.error, nextProps.validationDefinition);
    }
  }

  _showSpecialCharacterBase(parameter, validationMessage, level) {
    let rules = '<ul style="padding-left: 20px">';
    for (const ruleKey in parameter) {
      if (parameter.hasOwnProperty(ruleKey)) {
        rules += _.size(parameter) === 1 ? '<span><li>' + parameter[ruleKey] + '</li>' : '<span><li>' + ruleKey + ':  ' + parameter[ruleKey] + '</li>';
      }
    }
    rules += '</ul></span>';
    validationMessage.push(
      <Basic.Alert level={level} className="no-margin">
        <span dangerouslySetInnerHTML={{
          __html: this.i18n('content.passwordPolicies.validation.specialCharacterBase') + ' ' + rules}}
        />
      </Basic.Alert>);
  }

  /**
   * Method prepare error from password validation.
   * Only validation message is required, other will be skiped.
   */
  _prepareValidationMessage(error, validationDefinition) {
    if (!error || !error.parameters) {
      return null;
    }
    // For pre validate it shows as info (blue)
    let levelWarning = `warning`;
    let levelDanger = `danger`;
    if (validationDefinition) {
      levelWarning = `info`;
      levelDanger = `info`;
    }
    const validationMessage = [];
    let policies = '';

    // iterate over all parameters in error
    for (const key in error.parameters) {
      if (key === SPECIAL_CHARACTER_BASE) {
        this._showSpecialCharacterBase(error.parameters[key], validationMessage, levelWarning);
      }
      // error prameters must contain key and VALIDATION_WARNINGS must also contain key
      if (error.parameters.hasOwnProperty(key) && _.indexOf(VALIDATION_WARNINGS, key) !== -1) {
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
            <Basic.Alert level={levelWarning} className="no-margin">
              <span dangerouslySetInnerHTML={{
                __html: this.i18n('content.passwordPolicies.validation.' + MIN_RULES_TO_FULFILL, {'count': error.parameters[MIN_RULES_TO_FULFILL_COUNT]} ) + ' ' + rules}}
              />
            </Basic.Alert>);
        } else if (key !== PASSWORD_POLICIES_NAMES) {
          // set all attributes as parameter zero
          // validation message with date
          if (key === DATE) {
            validationMessage.push(
              <Basic.Alert level={levelWarning} className="no-margin">
                {this.i18n('content.passwordPolicies.validation.' + key, { 0: <DateValue value={error.parameters[key]}/> } )}
              </Basic.Alert>
            );
          } else {
            // other validation messages
            validationMessage.push(
              <Basic.Alert level={levelWarning} className="no-margin">
                {this.i18n('content.passwordPolicies.validation.' + key, { 0: error.parameters[key] })}
              </Basic.Alert>
            );
          }
        }
      }
    }
    // first message is password policies names, with danger class
    if (error.parameters.hasOwnProperty(PASSWORD_POLICIES_NAMES) || error.parameters.hasOwnProperty(POLICY_NAME_PREVALIDATION)) {
      policies = error.parameters.hasOwnProperty(PASSWORD_POLICIES_NAMES) ? this.i18n('content.passwordPolicies.validation.' + PASSWORD_POLICIES_NAMES) + error.parameters[PASSWORD_POLICIES_NAMES] : this.i18n('content.passwordPolicies.validation.' + POLICY_NAME_PREVALIDATION) + error.parameters[POLICY_NAME_PREVALIDATION];
      validationMessage.unshift(
        <Basic.Alert level={levelDanger} className="no-margin">
          {policies}
        </Basic.Alert>
      );
    }
    //
    return validationMessage;
  }

  render() {
    const { rendered, error, validationDefinition } = this.props;
    if (!rendered || !error) {
      return null;
    }

    const validation = this._prepareValidationMessage(error, validationDefinition);

    return (
      <div>
        {validation}
      </div>
    );
  }
}

ValidationMessage.propTypes = {
  ...Basic.AbstractFormComponent.propTypes,
  error: React.PropTypes.object,
  validationDefinition: React.PropTypes.object
};

ValidationMessage.defaultProps = {
  ...Basic.AbstractFormComponent.defaultProps
};
