

import { PropTypes } from 'react';
import * as Basic from '../../basic';

export default class FilterSelectBox extends Basic.SelectBox {

}

FilterSelectBox.propTypes = {
  ...Basic.SelectBox.propTypes,
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
const { labelSpan, componentSpan, ...otherDefaultProps } = Basic.SelectBox.defaultProps; // labelSpan etc. override
FilterSelectBox.defaultProps = {
  ...otherDefaultProps,
  relation: 'EQ',
  labelSpan: 'col-lg-4',
  componentSpan: 'col-lg-8'
};
