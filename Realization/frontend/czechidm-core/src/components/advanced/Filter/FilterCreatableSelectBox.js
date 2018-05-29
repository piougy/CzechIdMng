

import { PropTypes } from 'react';
import * as Basic from '../../basic';

export default class FilterCreatableSelectBox extends Basic.CreatableSelectBox {

}

FilterCreatableSelectBox.propTypes = {
  ...Basic.EnumSelectBox.propTypes,
  /**
   * Field name - if is not set, then ref is used
   * @type {[type]}
   */
  field: PropTypes.string,
  /**
   * Filter relation
   * TODO: enumeration
   */
  relation: PropTypes.oneOf(['EQ'])
};
const { labelSpan, componentSpan, ...otherDefaultProps } = Basic.CreatableSelectBox.defaultProps; // labelSpan etc. override
FilterCreatableSelectBox.defaultProps = {
  ...otherDefaultProps,
  relation: 'EQ'
};
