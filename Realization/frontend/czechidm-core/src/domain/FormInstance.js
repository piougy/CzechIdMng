import _ from 'lodash';
import Immutable from 'immutable';

/**
 * Immutable EAV form instance (definition + values).
 * Every modify operation returns new cloned FormInstance with new values.
 *
 * @author Radek Tomiška
 */
export default class FormInstance {

  constructor(formDefinition, formValues = null, validationErrors = null) {
    this.definition = formDefinition;
    //
    // prepare attributes from given definition
    this.attributes = new Immutable.OrderedMap();
    if (formDefinition._embedded && formDefinition._embedded.formAttributes) {
      formDefinition._embedded.formAttributes.forEach(formAttribute => {
        this.attributes = this.attributes.set(formAttribute.code, formAttribute);
      });
    }
    if (formDefinition.formAttributes) {
      formDefinition.formAttributes.forEach(formAttribute => {
        this.attributes = this.attributes.set(formAttribute.code, formAttribute);
      });
    }
    //
    // prepare values
    this._setValues(formValues);
    //
    //  set validation errors
    this.validationErrors = validationErrors;
  }

  _clone() {
    return _.clone(this);
  }

  /**
   * Return form definition used for this instance
   *
   * @return {formDefinition}
   */
  getDefinition() {
    return this.definition;
  }

  /**
   * Return attribute definitions ordered by its seq
   *
   * @return {Immutable.OrderedMap} <attributeName, attribute>
   */
  getAttributes() {
    return this.attributes;
  }

  getValidationErrors() {
    return this.validationErrors;
  }

  /**
   * Returns filled attribute values (multivalues are oreded by its seq)
   *
   * @param  {string} attributeName [optional] all attributes (values) are returned otherwise
   * @return {arrayOf(FormValue)}
   */
  getValues(attributeName) {
    if (!attributeName) {
      let result = [];
      this.values.forEach(valueList => {
        result = _.concat(result, valueList.toArray());
      });
      return result;
    }
    if (!this.values.has(attributeName)) {
      return null;
    }
    return this.values.get(attributeName).toArray();
  }

  /**
   * Returns filled values as properties object (ConfigurationMap on BE is preferred)
   *
   * TODO: multiple properties
   *
   * @return {object} [description]
   */
  getProperties() {
    const values = this.getValues();
    const properties = {};
    values.forEach(value => {
      const attributeCode = value._embedded.formAttribute.code;
      //  multiple properties are joined by comma separator
      if (properties[attributeCode]
        && this.getAttributes().has(attributeCode)
        && !!this.getAttributes().get(attributeCode).multiple) {
        properties[attributeCode] += `,${ value.value }`;
      } else {
        properties[attributeCode] = value.value;
      }
    });
    //
    return properties;
  }

  /**
   * Returns the first filled value of given attribute or null
   *
   * @param  {string} attributeName
   * @return {formValue}
   */
  getSingleValue(attributeName) {
    if (!this.values.has(attributeName)) {
      return null;
    }
    return this.values.get(attributeName).first();
  }

  /**
   * Set form values
   *
   * @param {arrayOf(FormValue)} formValues
   */
  _setValues(formValues) {
    this.values = new Immutable.OrderedMap();
    if (formValues) {
      formValues.forEach(formValue => {
        const attributeCode = formValue._embedded.formAttribute.code;
        //
        const clonedFormValue = _.clone(formValue);
        if (!this.values.has(attributeCode)) {
          this.values = this.values.set(attributeCode, new Immutable.List());
        }
        this.values = this.values.set(attributeCode, this.values.get(attributeCode).push(clonedFormValue));
      });
    }
  }

  /**
   * Set form values
   *
   * @param {arrayOf(FormValue)} formValues
   * @return {FormInstance} new instance
   */
  setValues(formValues) {
    const newState = this._clone();
    newState._setValues(formValues);
    //
    return newState;
  }

  /**
   * Set values by simple priperties object (key: value)
   *
   * @param {object} properties ConfigurationMap from BE is preferred
   * @return {FormInstance} new instance
   */
  setProperties(properties) {
    if (!properties) {
      return this.setValues(null);
    }
    // convert properties to form values
    const formValues = [];
    _.keys(properties).forEach(parameterName => {
      // value is used as fallback in renderers in concrete value by persistent type is not filled
      const value = properties[parameterName];
      // single or empty value
      if (!value
         || !this.getAttributes().has(parameterName)
         || !this.getAttributes().get(parameterName).multiple) {
        formValues.push({
          _embedded: {
            formAttribute: {
              code: parameterName
            }
          },
          value
        });
      } else if (_.isArray(value)) { // resolve multiple values
        value.forEach(singleValue => {
          formValues.push({
            _embedded: {
              formAttribute: {
                code: parameterName
              }
            },
            value: singleValue
          });
        });
      } else if (_.isString(value)) {
        value.split(',').forEach(singleValue => {
          formValues.push({
            _embedded: {
              formAttribute: {
                code: parameterName
              }
            },
            value: singleValue
          });
        });
      } else {
        LOGGER.error(`[FormInstance] property [${ parameterName }] value [${ value }] ` +
          `is not supported. Array or String type is supported only.`);
      }
    });
    //
    return this.setValues(formValues);
  }
}
