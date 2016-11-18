import _ from 'lodash';
import Immutable from 'immutable';

/**
 * EAV form instance (definitio + values)
 */
export default class FormInstance {

  constructor(formDefinition, formValues = null) {
    this.definition = formDefinition;
    //
    // prepare attributes from given definition
    this.attributes = new Immutable.OrderedMap();
    if (formDefinition._embedded && formDefinition._embedded.formAttributes) {
      formDefinition._embedded.formAttributes.forEach(formAttribute => {
        this.attributes = this.attributes.set(formAttribute.name, formAttribute);
      });
    }
    //
    // prepare values
    this.values = new Immutable.OrderedMap();
    if (formValues) {
      formValues.forEach(formValue => {
        const attributeName = formValue._embedded.formAttribute.name;
        //
        const clonedFormValue = _.clone(formValue);
        // link to attribute definition
        clonedFormValue.formAttribute = this.getAttributeLink(clonedFormValue._embedded.formAttribute.name);
        if (!this.values.has(attributeName)) {
          this.values = this.values.set(attributeName, new Immutable.List());
        }
        this.values = this.values.set(attributeName, this.values.get(attributeName).push(clonedFormValue));
      });
    }
  }

  _clone() {
    return _.clone(this);
  }

  getAttributeLink(attributeName) {
    return `/form-attributes/${this.attributes.get(attributeName).id}`;
  }

  getDefinition() {
    return this.definition;
  }

  getAttributes() {
    return this.attributes;
  }

  getValues() {
    return this.values;
  }

  getSingleValue(attributeName) {
    if (!this.values.has(attributeName)) {
      return null;
    }
    return this.values.get(attributeName).first();
  }
}
