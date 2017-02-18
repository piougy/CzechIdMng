import { PropTypes } from 'react';
//
import * as Basic from '../../basic';

export default class FilterBooleanSelectBox extends Basic.BooleanSelectBox {

}

FilterBooleanSelectBox.propTypes = {
  ...Basic.BooleanSelectBox.propTypes,
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
const { labelSpan, componentSpan, ...otherDefaultProps } = Basic.BooleanSelectBox.defaultProps; // labelSpan etc. override
FilterBooleanSelectBox.defaultProps = {
  ...otherDefaultProps,
  relation: 'EQ'
};
