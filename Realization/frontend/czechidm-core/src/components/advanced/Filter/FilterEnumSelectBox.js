

import { PropTypes } from 'react';
import * as Basic from '../../basic';

export default class FilterEnumSelectBox extends Basic.EnumSelectBox {

}

FilterEnumSelectBox.propTypes = {
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
  relation: PropTypes.oneOf(['EQ', 'NEQ'])
};
const { labelSpan, componentSpan, ...otherDefaultProps } = Basic.EnumSelectBox.defaultProps; // labelSpan etc. override
FilterEnumSelectBox.defaultProps = {
  ...otherDefaultProps,
  relation: 'EQ'
};
