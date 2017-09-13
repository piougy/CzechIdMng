import React, { PropTypes } from 'react';
import _ from 'lodash';
//
import * as Basic from '../../../components/basic';

/**
 * Abstract form value component
 * - supper class for all face type renderers
 *
 * @author Radek Tomiška
 */
export default class AbstractFormAttributeRenderer extends Basic.AbstractContextComponent {

  /**
   * Returns true, when confidential mode is supported
   *
   * @return {boolean}
   */
  supportsConfidential() {
    return false;
  }

  /**
   * Returns true, when multi value mode is supported
   *
   * @return {boolean}
   */
  supportsMultiple() {
    return false;
  }

  /**
   * Returns array of filled form values (form value object).
   * Single value attribute - array with one form velue
   * Multiple value attribute - array with form values
   *
   * @return {arrayOf(formValue)}
   */
  getValues() {
    const { attribute } = this.props;
    //
    if (attribute.multiple) {
      return this.getMultipleValue();
    }
    const filledFormValues = [];
    const formValue = this.getSingleValue();
    if (formValue) {
      filledFormValues.push(formValue);
    }
    return filledFormValues;
  }

  /**
   * Input value is valid
   *
   * @return {Boolean} is valid
   */
  isValid() {
    const formComponent = this.refs[AbstractFormAttributeRenderer.INPUT];
    if (!formComponent) {
      // not supported compoenents
      return true;
    }
    if (!formComponent.isValid()) {
      formComponent.setState({
        showValidationError: true
      });
      return false;
    }
    return true;
  }

  /**
   * Creates form value object. Preserve form value id attribute.
   *
   * @param  {[type]} formValue previously saved form valu
   * @param  {[type]} seq       value order (for multiple attrivbutes)
   * @return {FormValue}
   */
  prepareFormValue(formValue, seq = 0) {
    const { attribute } = this.props;
    //
    if (formValue === null) {
      formValue = {
        formAttribute: attribute.id
      };
    }
    formValue.seq = seq;
    return formValue;
  }

  /**
   * Returns joi validator by persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {Joi}
   */
  getInputValidation() {
    return null;
  }

  /**
   * Filled form value
   *
   * @return {FormValue}
   */
  getSingleValue() {
    const { values } = this.props;
    const formComponent = this.refs[AbstractFormAttributeRenderer.INPUT];
    //
    if (!formComponent) {
      // not supported compoenents
      return null;
    }
    const componentValue = formComponent.getValue();
    // undefined values are not sent (confidential properties  etc.)
    if (componentValue === undefined) {
      return null;
    }
    return this.fillFormValue(this.prepareFormValue(values ? values[0] : null), componentValue);
  }

  /**
   * Form values from multi input
   *
   * @return {arrayOf(FormValue)}
   */
  getMultipleValue() {
    throw new TypeError('Must override method getMultipleValue()');
  }

  /**
   * Fill form value field by persistent type from input value
   *
   * @param  {FormValue} formValue - form value
   * @param  {object} formComponent value
   * @return {FormValue}
   */
  fillFormValue(/* formValue, rawValue */) {
    throw new TypeError('Must override method fillFormValue()');
  }

  /**
   * Return value by attribute persistent type
   *
   * @param  {arrayOf(FormValue)} formValue form values array or single form value
   * @return {object} value by persistent type
   */
  toSingleInputValue(formValues) {
    const { attribute } = this.props;
    //
    let formValue = null;
    if (formValues && _.isArray(formValues)) {
      if (formValues.length > 0) {
        formValue = formValues[0];
      }
    } else {
      formValue = formValues;
    }
    if (formValue === null) {
      return attribute.defaultValue;
    }
    return this.getInputValue(formValue);
  }

  toMultipleInputValue(/* formValues*/) {
    throw new TypeError('Must override method toMultipleInputValue()');
  }

  /**
   * Returns value, which has to be set to input from given (persisted) form value
   *
   * @param  {FormValue} formValue
   * @return {object} value by persistent type
   */
  getInputValue(/* formValue*/) {
    throw new TypeError('Must override method getInputValue(formValue)');
  }

  /**
   * Unsupported info
   */
  _unsupportedMode(mode = 'single') {
    const { attribute } = this.props;
    const formDefinition = attribute._embedded.formDefinition;
    //
    return (
      <Basic.LabelWrapper label={ attribute.code } >
        <Basic.Alert level="warning" className="no-margin">
          <div>{ this.i18n(`component.advanced.EavForm.${mode}.unsupported.title`, { name: attribute.persistentType, face: attribute.faceType }) }</div>
          <div>{ this.i18n(`component.advanced.EavForm.${mode}.unsupported.formDefinition.title`) }:</div>
          <div style={{ wordWrap: 'break-word' }}>{ this.i18n(`component.advanced.EavForm.${mode}.unsupported.formDefinition.type`) }: { formDefinition.type }</div>
          <div style={{ wordWrap: 'break-word' }}>{ this.i18n(`component.advanced.EavForm.${mode}.unsupported.formDefinition.code`) }: { formDefinition.code }</div>
        </Basic.Alert>
      </Basic.LabelWrapper>
    );
  }

  /**
   * Input as single fields
   */
  renderSingleInput() {
    return this._unsupportedMode();
  }

  /**
   * Input for multiple values
   */
  renderMultipleInput() {
    return this._unsupportedMode('multiple');
  }

  render() {
    const { attribute } = this.props;
    // check confidential support
    if (attribute.confidential && !this.supportsConfidential()) {
      return (
        <Basic.LabelWrapper label={attribute.name}>
          <Basic.Alert level="warning" text={ this.i18n('component.advanced.EavForm.persistentType.unsupported.confidential', { name: attribute.persistentType}) } className="no-margin"/>
        </Basic.LabelWrapper>
      );
    }
    // multiple module is disabled, even when renderMultipleInput is overriden
    if (!this.supportsMultiple() && attribute.multiple) {
      return this._unsupportedMode('multiple');
    }
    return (attribute.multiple) ? this.renderMultipleInput() : this.renderSingleInput();
  }
}

AbstractFormAttributeRenderer.propTypes = {
  /**
   * Form attribute
   */
  attribute: PropTypes.object.isRequired,
  /**
   * Filled form values
   */
  values: PropTypes.arrayOf(PropTypes.object),
  /**
   * ReadOnly form field
   */
  readOnly: PropTypes.bool
};
AbstractFormAttributeRenderer.defaultProps = {
  readOnly: false
};

AbstractFormAttributeRenderer.INPUT = 'input'; // input ref - is used internally for common operations
