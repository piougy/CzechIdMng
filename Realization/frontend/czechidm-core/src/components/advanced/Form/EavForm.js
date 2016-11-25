import React, { PropTypes } from 'react';
import _ from 'lodash';
//
import * as Basic from '../../basic';

/**
 * Content of eav form by given form instance (= form definition + form values)
 *
 * TODO:
 * - multiple attributes
 * - textarea, richtextarea, date, datetime, long, double, currency attribute types (and appropriate validation)
 * - guarded string for confidential attributes
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

      if (attribute.multiple) {
        const formValues = formInstance.getValues(attribute.name) || [];
        // split multilines text = multi values
        const textValue = formComponent.getValue();
        if (textValue) {
          const textValues = textValue.split('\n');
          for (let i = 0; i < textValues.length; i++) {
            let formValue = null;
            if (i < formValues.length) {
              formValue = formValues[i];
            }
            filledFormValues.push(this._fillFormValue(formInstance, attribute, formValue, textValues[i], i));
          }
        }
      } else {
        // single value
        filledFormValues.push(this._fillFormValue(formInstance, attribute, formInstance.getSingleValue(attribute.name), formComponent.getValue()));
      }
    });
    return filledFormValues;
  }

  /**
   * Fill form value by persistent type
   *
   * @param  {FormInstance} formInstance
   * @param  {FormAttribute} attribute
   * @param  {FormValue} formValue
   * @param  {[type]} formComponent
   * @return {FormValue}
   */
  _fillFormValue(formInstance, attribute, formValue, rawValue, seq = 0) {
    if (formValue === null) {
      formValue = {
        formAttribute: formInstance.getAttributeLink(attribute.name)
      };
    }
    formValue.seq = seq;
    // set value by persistent type
    switch (attribute.persistentType) {
      case 'CHAR':
      case 'TEXT':
      case 'TEXTAREA': {
        formValue.stringValue = rawValue;
        break;
      }
      case 'INT':
      case 'LONG': {
        formValue.longValue = rawValue;
        break;
      }
      case 'BOOLEAN': {
        formValue.booleanValue = rawValue;
        break;
      }
      default: {
        this.getLogger().warn(`[EavForm]: Persistent type [${attribute.persistentType}] is not supported and not be filled and send to BE!`);
      }
    }
    return formValue;
  }

  /**
   * Return value by attribute persistent type
   *
   * @param  {FormAttrinute} attribute attribute definition
   * @param  {FormValue} formValue form value
   * @return {oneOf([string, boolean, long])}
   */
  _toInputValue(attribute, formValue) {
    if (formValue === null) {
      return attribute.defaultValue;
    }
    if (_.isArray(formValue)) {
      // multi values are transformed to multi lines
      let result = null;
      formValue.forEach(singleValue => {
        if (result !== null) {
          result += '\n';
        }
        const inputValue = this._toInputValue(attribute, singleValue);
        if (inputValue) {
          if (result === null) { // single values should not be concated
            result = inputValue;
          } else {
            result += inputValue;
          }
        }
      });
      return result;
    }
    //
    switch (attribute.persistentType) {
      case 'CHAR':
      case 'TEXT':
      case 'TEXTAREA': {
        return formValue.stringValue;
      }
      case 'INT':
      case 'LONG': {
        return formValue.longValue;
      }
      case 'BOOLEAN': {
        return formValue.booleanValue;
      }
      default: {
        this.getLogger().warn(`[EavForm]: Persistent type [${attribute.persistentType}] is not supported and not be filled and send to BE!`);
      }
    }
    return null;
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
            const formValues = formInstance.getValues(attribute.name);
            //
            if (attribute.multiple) {
              // unsupported variant
              if (attribute.persistentType === 'TEXTAREA' || attribute.persistentType === 'BOOLEAN') {
                return (
                  <Basic.LabelWrapper label={attribute.displayName} >
                    <Basic.Alert level="warning" className="no-margin">
                      <div>{attribute.persistentType } - attribute can not be multiple.</div>
                      <div>Form definition has to be fixed:</div>
                      <div style={{ wordWrap: 'break-word' }}>Type: {formInstance.getDefinition().type}</div>
                      <div style={{ wordWrap: 'break-word' }}>Name: {formInstance.getDefinition().name}</div>
                    </Basic.Alert>
                  </Basic.LabelWrapper>
                );
              }
              // multi values are presented as multi lines string
              // TODO: localization
              return (
                <Basic.TextArea
                  ref={attribute.name}
                  type={attribute.confidential ? 'password' : 'text'}
                  required={attribute.required}
                  label={`${attribute.displayName} (multi)`}
                  value={this._toInputValue(attribute, formValues)}
                  helpBlock={attribute.description ? attribute.description : 'Every value is on new line'}
                  readOnly={attribute.readonly}/>
              );
            }
            //
            // single field
            // TODO: validation
            if (attribute.persistentType === 'TEXT'
              || attribute.persistentType === 'CHAR'
              || attribute.persistentType === 'INT'
              || attribute.persistentType === 'LONG'
              || attribute.persistentType === 'DOUBLE') {
              return (
                <Basic.TextField
                  ref={attribute.name}
                  type={attribute.confidential ? 'password' : 'text'}
                  required={attribute.required}
                  label={attribute.displayName}
                  value={this._toInputValue(attribute, formValues)}
                  helpBlock={attribute.description}
                  readOnly={attribute.readonly}/>
              );
            }
            // textarea field
            if (attribute.persistentType === 'TEXTAREA') {
              return (
                <Basic.TextArea
                  ref={attribute.name}
                  required={attribute.required}
                  label={attribute.displayName}
                  value={this._toInputValue(attribute, formValues)}
                  helpBlock={attribute.description}
                  readOnly={attribute.readonly}/>
              );
            }
            // boolean field - boolean can not be multiple
            if (attribute.persistentType === 'BOOLEAN') {
              return (
                <Basic.Checkbox
                  ref={attribute.name}
                  label={attribute.displayName}
                  value={formValues ? this._toInputValue(attribute, formValues) : (attribute.defaultValue === 'true')}
                  helpBlock={attribute.description}
                  readOnly={attribute.readonly}/>
              );
            }
            return (
              <Basic.LabelWrapper label={attribute.displayName}>
                <Basic.Alert level="warning" text={`${attribute.persistentType } - unimplemented persistentType`} className="no-margin"/>
              </Basic.LabelWrapper>
            );
          })
        }
      </span>
    );
  }
}

EavForm.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * FormInstance (definition + values)
   */
  formInstance: PropTypes.object
};
EavForm.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  formInstance: null
};
