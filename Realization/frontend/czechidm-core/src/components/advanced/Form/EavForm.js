import React, { PropTypes } from 'react';
//
import * as Basic from '../../basic';

/**
 * Content of eav form by given form instance (= form definition + form values)
 *
 * TODO:
 * - multiple attributes
 * - textarea, richtextarea, date, datetime, long, double, currency attribute types (and appropriate validation)
 */
export default class EavForm extends Basic.AbstractContextComponent {

  /**
   * Returns true, when form is valid, otherwise false
   *
   * @return {Boolean} [description]
   */
  isValid() {
    const { formInstance } = this.props;
    let isAllValid = true;
    formInstance.getAttributes().forEach(attribute => {
      const formComponent = this.refs[attribute.name];
      // we need to call validate method on all component (break is not needed)
      if (!formComponent.validate()) {
        isAllValid = false;
      }
    });
    //
    return isAllValid;
  }

  /**
   * Return form definition used for form instance
   *
   * @return {formDefinition}
   */
  getFormDefinition() {
    const { formInstance } = this.props;
    //
    if (!formInstance) {
      return null;
    }
    return formInstance.getDefinition();
  }

  /**
   * Returns array of filled form values (form value object)
   *
   * @return {arrayOf(formValue)}
   */
  getValues() {
    const { formInstance } = this.props;
    const filledFormValues = [];
    //
    formInstance.getAttributes().forEach(attribute => {
      const formComponent = this.refs[attribute.name];
      let formValue = formInstance.getSingleValue(attribute.name);
      if (formValue === null) {
        // construct form value
        formValue = {
          formAttribute: formInstance.getAttributeLink(attribute.name)
        };
      }
      // set value by persistent type
      switch (attribute.persistentType) {
        case 'TEXT': {
          formValue.stringValue = formComponent.getValue();
          break;
        }
        case 'BOOLEAN': {
          formValue.booleanValue = formComponent.getValue();
          break;
        }
        default: {
          this.getLogger().warn(`[EavForm]: Persistent type [${attribute.persistentType}] is not supported and not be filled and send to BE!`);
        }
      }
      filledFormValues.push(formValue);
    });
    return filledFormValues;
  }


  render() {
    const { formInstance, rendered, showLoading } = this.props;
    //
    if (!rendered || !formInstance) {
      return null;
    }
    //
    if (showLoading) {
      return (
        <Basic.Loading isStatic showLoading/>
      );
    }

    return (
      <span>
        {
          formInstance.getAttributes().map(attribute => {
            const formValue = formInstance.getSingleValue(attribute.name);
            // text field
            if (attribute.persistentType === 'TEXT') {
              return (
                <Basic.TextField
                  ref={attribute.name}
                  required={attribute.required}
                  label={attribute.displayName}
                  value={formValue ? formValue.stringValue : null}
                  helpBlock={attribute.description}/>
              );
            }
            // boolean field - boolean can not be multiple
            if (attribute.persistentType === 'BOOLEAN') {
              return (
                <Basic.Checkbox
                  ref={attribute.name}
                  label={attribute.displayName}
                  value={formValue ? formValue.booleanValue : (attribute.defaultValue === 'true')}
                  helpBlock={attribute.description}/>
              );
            }
            return (
              <div>Unimplemented persistentType: { attribute.persistentType }</div>
            );
          })
        }
      </span>
    );
  }
}

EavForm.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  formInstance: PropTypes.object
};
EavForm.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  formInstance: null
};
