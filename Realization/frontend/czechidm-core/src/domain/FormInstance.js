import _ from 'lodash';
import Immutable from 'immutable';

/**
 * EAV form instance (definition + values)
 *
 * @author Radek Tomiška
 */
export default class FormInstance {

  constructor(formDefinition, formValues = null) {
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

  /**
   * Returns filled attribute values (multivalues are oreded by its seq)
   *
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
   * Returns the first filled value of given attribute or null
   *
   * @param  {striung} attributeName
   * @return {formValue}
   */
  getSingleValue(attributeName) {
    if (!this.values.has(attributeName)) {
      return null;
    }
    return this.values.get(attributeName).first();
  }
}
