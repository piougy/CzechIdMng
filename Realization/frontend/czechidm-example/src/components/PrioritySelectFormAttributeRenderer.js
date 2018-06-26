import React from 'react';
//
import { Advanced } from 'czechidm-core';

/**
 * Priority form value component. Does'nt support:
 * - multiple
 * - confidential
 * - placeholder
 * - helpBlock
 * - validation
 * - required
 *
 * @author Radek Tomiška
 */
export default class PrioritySelectFormAttributeRenderer extends Advanced.AbstractFormAttributeRenderer {

  constructor(props, context) {
    super(props, context);
    // prepare radio value
    this.state = {
      value: null
    };
  }

  /**
   * Fill form value field by persistent type from input value
   *
   * @param  {FormValue} formValue - form value
   * @param  {object} formComponent value
   * @return {FormValue}
   */
  fillFormValue(formValue, rawValue) {
    formValue.longValue = rawValue;
    // common value can be used without persistent type knowlege (e.g. conversion to properties object)
    formValue.value = formValue.longValue;
    //
    // TODO: validations for numbers
    return formValue;
  }

  /**
   * Returns value to ipnut from given (persisted) form value
   *
   * @param  {FormValue} formValue
   * @return {object} value by persistent type
   */
  getInputValue(formValue) {
    return formValue.longValue;
  }

  /**
   * Filled form value
   *
   * @return {FormValue}
   */
  toFormValue() {
    const { values } = this.props;
    //
    return this.fillFormValue(this.prepareFormValue(values ? values[0] : null), this.state.value);
  }

  /**
   * Set value on radio is clicked (changed)
   *
   * @param {event} event
   */
  setValue(event) {
    this.setState({
      value: event.target.value
    });
  }

  renderSingleInput() {
    const { attribute, readOnly, values } = this.props;
    const singleValue = this.state.value || this.toInputValue(values);
    // create radio inputs
    const inputs = [];
    for (let i = 1; i <= PrioritySelectFormAttributeRenderer.RADIO_COUNT; i++) {
      inputs.push(
        <label className="radio-inline">
          <input
            name={ attribute.name }
            type="radio"
            readOnly={ readOnly || attribute.readonly }
            value={ i }
            defaultChecked={ singleValue === i }/> { i }
        </label>
      );
    }
    // raw bootstrap styles are used in this example (Basic radio component should be created instead)
    return (
      <div className="form-group">
        <label className="control-label">{ attribute.name }</label>
        <div className="radio" onChange={this.setValue.bind(this)}>
          { inputs }
        </div>
      </div>
    );
  }
}

PrioritySelectFormAttributeRenderer.RADIO_COUNT = 5;
