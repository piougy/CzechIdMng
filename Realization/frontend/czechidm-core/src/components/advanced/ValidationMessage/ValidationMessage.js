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
 * Merge similar password lines
 * @type {String}
 */
const PWD_SIMILAR = 'passwordSimilarPreValidate';

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

  _showSpecialCharacterBase(parameter) {
    let rules = '<ul style="padding-left: 20px">';
    for (const ruleKey in parameter) {
      if (parameter.hasOwnProperty(ruleKey)) {
        rules += _.size(parameter) === 1 ? '<span><li>' + parameter[ruleKey] + '</li>' : '<span><li>' + ruleKey + ':  ' + parameter[ruleKey] + '</li>';
      }
    }
    rules += '</ul></span>';
    return this.i18n('content.passwordPolicies.validation.specialCharacterBase') + ' ' + rules;
  }

  /**
   * Method prepare error from password validation.
   * Only validation message is required, other will be skiped.
   */
  _prepareValidationMessage(error) {
    if (!error || !error.parameters) {
      return null;
    }
    // For pre validate it shows as info (blue)
    const levelWarning = `warning`;
    const levelDanger = `danger`;

    const validationMessage = [];

    // iterate over all parameters in error
    for (const key in error.parameters) {
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
    if (error.parameters.hasOwnProperty(PASSWORD_POLICIES_NAMES)) {
      validationMessage.unshift(
        <Basic.Alert level={levelDanger} className="no-margin">
          {this.i18n('content.passwordPolicies.validation.' + PASSWORD_POLICIES_NAMES) + error.parameters[PASSWORD_POLICIES_NAMES]}
        </Basic.Alert>
      );
    }
    //
    return validationMessage;
  }

  _preparePreValidationMessage(error) {
    if (!error || !error.parameters) {
      return null;
    }

    const validationMessage = [];
    let lines = '<ul style="padding-left: 20px">'; // every one of these rules must be met
    let rules = ''; // one of two following rules must be met....
    let result = ''; // for merging lines, rules and char base
    let charBase = ''; // for shown special character base
    const similar = []; // for merging pwd must not be similar to name, mail, username

    // iterate over all parameters in error
    for (const key in error.parameters) {
      if (key === SPECIAL_CHARACTER_BASE) {
        charBase = this._showSpecialCharacterBase(error.parameters[key], validationMessage, `info`);
      }
      // error prameters must contain key and VALIDATION_WARNINGS must also contain key
      if (error.parameters.hasOwnProperty(key) && _.indexOf(VALIDATION_WARNINGS, key) !== -1) {
        // enchanced control special message, minimal rules to fulfill
        if (key === MIN_RULES_TO_FULFILL) {
          // fill rules with not required messages
          rules = '<ul style="padding-left: 20px">';
          for (const ruleKey in error.parameters[key]) {
            if (error.parameters[key].hasOwnProperty(ruleKey)) {
              rules += '<span><li>' + this.i18n('content.passwordPolicies.validation.' + ruleKey) + error.parameters[key][ruleKey] + '</li>';
            }
          }
          rules += '</ul></span>';
          rules = this.i18n('content.passwordPolicies.validation.' + MIN_RULES_TO_FULFILL, {'count': error.parameters[MIN_RULES_TO_FULFILL_COUNT]} ) + ' ' + rules;
          // to merge - pwd must not be similar to name, mail, username
        } else if (key === 'passwordSimilarUsernamePreValidate' || key === 'passwordSimilarEmailPreValidate' || key === 'passwordSimilarFirstNamePreValidate' || key === 'passwordSimilarLastNamePreValidate') {
          similar.push(this.i18n('content.passwordPolicies.validation.' + key));
        } else if ( key !== MIN_RULES_TO_FULFILL_COUNT ) {
          // other validation messages
          lines += '<span><li>' + this.i18n('content.passwordPolicies.validation.' + key, { 0: error.parameters[key] }) + '</li>';
        }
      }
    }
    if (similar.length > 0) {
      lines += '<span><li>' + this.i18n('content.passwordPolicies.validation.' + PWD_SIMILAR) + ' ' + this._toMyString(similar) + '.';
    }
    lines += '</ul></span>';
    result = lines + rules + charBase;
    validationMessage.push(
      <Basic.Alert level={'info'} className="no-margin last">
        <span dangerouslySetInnerHTML={{ __html: result }} />
      </Basic.Alert>
    );
    return validationMessage;
  }

  /**
   * Method prepare nice toString of items with comma and space
   */
  _toMyString(similar) {
    let items = similar[0];
    let i;
    for ( i = 1; i < similar.length; i++) {
      items += ', ' + similar[i];
    }
    return items;
  }

  render() {
    const { rendered, error, validationDefinition } = this.props;
    if (!rendered || !error) {
      return null;
    }

    let validation;
    if (validationDefinition) {
      validation = this._preparePreValidationMessage(error);
    } else {
      validation = this._prepareValidationMessage(error);
    }

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
