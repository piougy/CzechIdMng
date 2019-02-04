import React from 'react';
import classNames from 'classnames';
//
import { Advanced } from 'czechidm-core';

/**
 * Priority form value component. Doesn't support:
 * - multiple
 * - confidential
 * - placeholder
 * - helpBlock
 * - validation
 * - required
 * - validationErrors
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

  renderSingleInput(originalValues) {
    const { attribute, readOnly, values, className, style } = this.props;
    const showOriginalValue = originalValues ? true : false;
    const singleValue = this.state.value || this.toInputValue(showOriginalValue ? originalValues : values);
    // create radio inputs
    const inputs = [];
    for (let i = 1; i <= PrioritySelectFormAttributeRenderer.RADIO_COUNT; i++) {
      inputs.push(
        <label className="radio-inline">
          <input
            name={ attribute.name }
            type="radio"
            readOnly={ showOriginalValue ? true : (readOnly || attribute.readonly) }
            value={ i }
            defaultChecked={ singleValue === i }/> { i }
        </label>
      );
    }
    // raw bootstrap styles are used in this example (Basic radio component should be created instead)
    return (
      <div className={ classNames(className, 'form-group') } style={ style }>
        <label className="control-label">{ this.getLabel(null, showOriginalValue) }</label>
        <div className="radio" onChange={this.setValue.bind(this)}>
          { inputs }
        </div>
      </div>
    );
  }
}

PrioritySelectFormAttributeRenderer.RADIO_COUNT = 5;
