import moment from 'moment';
//
import * as Domain from '../../../domain';
import * as Basic from '../../basic';
import * as Utils from '../../../utils';

/**
 * Content with eav attributes.
 *
 * @author Radek Tomiška
 * @since 11.0.0
 */
export default class AbstractFormableContent extends Basic.AbstractContent {

  getBasicAttributesFormInstance(entity) {
    if (!entity || !entity._eav) {
      return null;
    }
    const formInstance = entity._eav.find(i => {
      return i.formDefinition.code === 'idm:basic-fields';
    });
    //
    if (!formInstance) {
      return null;
    }
    return new Domain.FormInstance(formInstance);
  }

  getBasicField(formInstance, attributeName) {
    if (!formInstance) {
      return null;
    }
    //
    return formInstance.getAttributes().get(attributeName);
  }

  /**
   * Basic field label.
   *
   * @since 11.1.0
   */
  getLabel(formInstance, attributeName, label = null) {
    const attribute = this.getBasicField(formInstance, attributeName);
    if (!attribute) {
      return label;
    }
    //
    return this.i18n(attribute.label) || label;
  }

  /**
   * Basic field placeholder.
   *
   * @since 11.1.0
   */
  getPlaceholder(formInstance, attributeName, placeholder = null) {
    const attribute = this.getBasicField(formInstance, attributeName);
    if (!attribute) {
      return placeholder;
    }
    //
    return this.i18n(attribute.placeholder) || placeholder;
  }

  isReadOnly(formInstance, attributeName, readOnly = false) {
    const attribute = this.getBasicField(formInstance, attributeName);
    if (!attribute) {
      return readOnly;
    }
    //
    return readOnly || attribute.readonly;
  }

  isRequired(formInstance, attributeName, readOnly = false) {
    const attribute = this.getBasicField(formInstance, attributeName);
    if (!attribute) {
      return false;
    }
    if (this.isReadOnly(formInstance, attributeName, readOnly)) {
      return false;
    }
    //
    return attribute.required;
  }

  getMin(formInstance, attributeName, readOnly = false, minValueRange = null) {
    const attribute = this.getBasicField(formInstance, attributeName);
    if (!attribute || readOnly) {
      return minValueRange;
    }
    let min = attribute.min;
    // not configured
    if (Utils.Ui.isEmpty(min)) {
      return minValueRange;
    }
    min = parseInt(min, 10);
    if (Utils.Ui.isEmpty(min)) {
      return minValueRange;
    }
    if (Utils.Ui.isEmpty(minValueRange)) {
      return min;
    }
    //
    return min < minValueRange ? minValueRange : min;
  }

  getMax(formInstance, attributeName, readOnly = false, maxValueRange = null) {
    const attribute = this.getBasicField(formInstance, attributeName);
    if (!attribute || readOnly) {
      return maxValueRange;
    }
    //
    let max = attribute.max;
    // not configured
    if (Utils.Ui.isEmpty(max)) {
      return maxValueRange;
    }
    max = parseInt(max, 10);
    if (Utils.Ui.isEmpty(max)) {
      return maxValueRange;
    }
    if (Utils.Ui.isEmpty(maxValueRange)) {
      return max;
    }
    //
    return max > maxValueRange ? maxValueRange : max;
  }

  getMinDate(formInstance, attributeName, readOnly = false) {
    const minDays = this.getMin(formInstance, attributeName, readOnly);
    // not configured
    if (Utils.Ui.isEmpty(minDays) || readOnly) {
      return null;
    }
    //
    return moment().add(minDays, 'days');
  }

  getMaxDate(formInstance, attributeName, readOnly = false) {
    const maxDays = this.getMax(formInstance, attributeName, readOnly);
    // not configured
    if (Utils.Ui.isEmpty(maxDays) || readOnly) {
      return null;
    }
    //
    return moment().add(maxDays, 'days');
  }

  getValidationMessage(formInstance, attributeName) {
    const attribute = this.getBasicField(formInstance, attributeName);
    if (!attribute) {
      return false;
    }
    //
    return attribute.validationMessage;
  }

  getInvalidBasicField(validationErrors, attributeCode) {
    if (!validationErrors) {
      return [];
    }
    //
    return validationErrors
      .filter(attribute => { // by attribute code
        return attribute.attributeCode === attributeCode;
      });
  }
}

AbstractFormableContent.propTypes = {
  ...Basic.AbstractContent.propTypes
};

AbstractFormableContent.defaultProps = {
  ...Basic.AbstractContent.defaultProps
};
